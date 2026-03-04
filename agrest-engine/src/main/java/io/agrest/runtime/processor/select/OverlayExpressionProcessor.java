package io.agrest.runtime.processor.select;

import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.exp.parser.AgExpressionParserDefaultVisitor;
import io.agrest.exp.parser.ExpPath;
import io.agrest.exp.parser.SimpleNode;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.ConditionalRelationshipOverlay;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Sort;
import io.agrest.runtime.meta.RequestSchema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Processes conditional relationship overlays by:
 * <ul>
 *     <li>Annotating expression paths with aliases (virtual -> physical relationship names).</li>
 *     <li>Injecting overlay qualifiers into entity expressions.</li>
 *     <li>Prefixing overlay qualifier paths with overlay relationship names.</li>
 * </ul>
 *
 * @since 5.0
 */
public class OverlayExpressionProcessor {

    public void process(ResourceEntity<?> entity, RequestSchema schema) {
        if (schema == null) {
            return;
        }

        processEntity(entity, schema);

        for (ResourceEntity<?> child : entity.getChildren()) {
            process(child, schema);
        }
    }

    private void processEntity(ResourceEntity<?> entity, RequestSchema schema) {
        AgEntity<?> agEntity = entity.getAgEntity();

        if (entity.getExp() != null) {
            annotateAliases(entity.getExp(), agEntity, schema);
        }

        List<Exp> qualifiers = collectOverlayQualifiers(entity, agEntity, schema);
        if (!qualifiers.isEmpty()) {
            Exp combined = Exp.and(qualifiers.toArray(new Exp[0]));
            entity.andExp(combined);
        }
    }

    private void annotateAliases(Exp exp, AgEntity<?> agEntity, RequestSchema schema) {
        exp.accept(new AgExpressionParserDefaultVisitor<Void>() {
            @Override
            public Void visit(ExpPath node, Void data) {
                String path = node.getPath();
                Map<String, String> aliases = resolveAliases(path, agEntity, schema);
                if (!aliases.isEmpty()) {
                    node.setPathAliases(aliases);
                }
                return super.visit(node, data);
            }
        }, null);
    }

    private Map<String, String> resolveAliases(String path, AgEntity<?> agEntity, RequestSchema schema) {
        if (schema == null) {
            return Collections.emptyMap();
        }

        Map<String, String> aliases = new HashMap<>();
        String[] segments = path.split("\\.");
        AgEntity<?> currentEntity = agEntity;

        for (int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];
            ConditionalRelationshipOverlay conditional = ConditionalRelationshipOverlay.resolveOverlay(schema, currentEntity, segment);
            if (conditional != null) {
                aliases.put(segment, conditional.getUnderlyingRelationshipName());
                AgRelationship underlying = currentEntity.getRelationship(conditional.getUnderlyingRelationshipName());
                currentEntity = underlying.getTargetEntity();
                continue;
            }

            AgRelationship relationship = currentEntity.getRelationship(segment);
            if (relationship != null) {
                currentEntity = relationship.getTargetEntity();
            } else {
                break;
            }
        }

        return aliases;
    }

    private List<Exp> collectOverlayQualifiers(ResourceEntity<?> entity, AgEntity<?> agEntity, RequestSchema schema) {
        List<Exp> qualifiers = new ArrayList<>();

        // From sort paths
        for (Sort sort : entity.getOrderings()) {
            String path = sort.getPath();
            Map<String, String> aliases = resolveAliases(path, agEntity, schema);
            if (!aliases.isEmpty()) {
                entity.setSortPathAliases(path, aliases);
            }
            
            Exp qualifier = overlayQualifier(path, agEntity, schema);
            if (qualifier != null) {
                qualifiers.add(qualifier);
            }
        }

        // From expression paths
        if (entity.getExp() != null) {
            collectFromExpression(entity.getExp(), agEntity, schema, qualifiers);
        }

        // From incoming relationship (for related entities)
        if (entity instanceof RelatedResourceEntity<?>) {
            Exp incoming = incomingQualifier((RelatedResourceEntity<?>) entity, schema);
            if (incoming != null) {
                qualifiers.add(incoming);
            }
        }

        Set<Exp> unique = new LinkedHashSet<>(qualifiers);
        return new ArrayList<>(unique);
    }

    private void collectFromExpression(Exp exp, AgEntity<?> agEntity, RequestSchema schema, List<Exp> qualifiers) {
        exp.accept(new AgExpressionParserDefaultVisitor<Void>() {
            @Override
            public Void visit(ExpPath node, Void data) {
                Exp qualifier = overlayQualifier(node.getPath(), agEntity, schema);
                if (qualifier != null) {
                    qualifiers.add(qualifier);
                }
                return super.visit(node, data);
            }
        }, null);
    }

    private Exp overlayQualifier(String path, AgEntity<?> agEntity, RequestSchema schema) {
        int dotIndex = path.indexOf('.');
        String firstSegment = dotIndex > 0 ? path.substring(0, dotIndex) : path;

        ConditionalRelationshipOverlay overlay = ConditionalRelationshipOverlay.resolveOverlay(schema, agEntity, firstSegment);
        if (overlay == null || overlay.getQualifier() == null) {
            return null;
        }

        if (dotIndex < 0 && agEntity.getRelationship(firstSegment) == null) {
            return null;
        }

        return prefixQualifierPaths(overlay.getQualifier(), firstSegment, overlay.getUnderlyingRelationshipName());
    }

    private Exp incomingQualifier(RelatedResourceEntity<?> entity, RequestSchema schema) {
        ConditionalRelationshipOverlay incoming = ConditionalRelationshipOverlay
                .resolveOverlay(schema, entity.getParent().getAgEntity(), entity.getIncoming().getName());
        if (incoming == null || incoming.getQualifier() == null) {
            return null;
        }

        return incoming.getQualifier();
    }

    private Exp prefixQualifierPaths(Exp qualifier, String prefix, String underlyingName) {
        Map<String, String> aliases = Map.of(prefix, underlyingName);

        SimpleNode copied = ((SimpleNode) qualifier).deepCopy();
        copied.accept(new AgExpressionParserDefaultVisitor<Void>() {
            @Override
            public Void visit(ExpPath node, Void data) {
                String newPath = prefix + "." + node.getPath();
                node.setPath(newPath);
                node.setPathAliases(aliases);
                return super.visit(node, data);
            }
        }, null);
        return copied;
    }
}
