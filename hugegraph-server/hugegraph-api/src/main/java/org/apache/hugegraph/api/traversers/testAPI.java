package org.apache.hugegraph.api.traversers;
import static org.apache.hugegraph.traversal.algorithm.HugeTraverser.DEFAULT_ELEMENTS_LIMIT;
import static org.apache.hugegraph.traversal.algorithm.HugeTraverser.DEFAULT_MAX_DEGREE;
import static org.apache.hugegraph.traversal.algorithm.HugeTraverser.NO_LIMIT;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.api.graph.EdgeAPI;
import org.apache.hugegraph.api.graph.VertexAPI;
import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.backend.query.QueryResults;
import org.apache.hugegraph.core.GraphManager;
import org.apache.hugegraph.structure.HugeVertex;
import org.apache.hugegraph.traversal.algorithm.HugeTraverser;
import org.apache.hugegraph.traversal.algorithm.KneighborTraverser;
import org.apache.hugegraph.traversal.algorithm.test;
import org.apache.hugegraph.traversal.algorithm.records.KneighborRecords;
import org.apache.hugegraph.traversal.algorithm.steps.Steps;
import org.apache.hugegraph.type.define.Directions;
import org.apache.hugegraph.util.E;
import org.apache.hugegraph.util.Log;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.slf4j.Logger;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
@Path("graphs/{graph}/traversers/test")
@Singleton
@Tag(name = "testAPI")
public class testAPI extends TraverserAPI{
    @GET
    @Timed
    @Produces(APPLICATION_JSON_WITH_CHARSET)
    public String get(@Context GraphManager manager,
                      @PathParam("graph") String graph,
                      @QueryParam("source") String sourceV,
                      @QueryParam("direction") String direction,
                      @QueryParam("label") String edgeLabel,
                      @QueryParam("max_depth") int depth,
                      @QueryParam("count_only")
                      @DefaultValue("false") boolean countOnly,
                      @QueryParam("max_degree")
                      @DefaultValue(DEFAULT_MAX_DEGREE) long maxDegree,
                      @QueryParam("limit")
                      @DefaultValue(DEFAULT_ELEMENTS_LIMIT) int limit) {
        LOG.debug("Graph [{}] get k-neighbor from '{}' with " +
                        "direction '{}', edge label '{}', max depth '{}', " +
                        "max degree '{}' and limit '{}'",
                graph, sourceV, direction, edgeLabel, depth,
                maxDegree, limit);

        ApiMeasurer measure = new ApiMeasurer();

        Id source = VertexAPI.checkAndParseVertexId(sourceV);
        Direction dir = EdgeAPI.parseDirection(direction);
        //Directions dir = Directions.convert(EdgeAPI.parseDirection(direction));


        HugeGraph g = graph(manager, graph);

        Set<Id> ids;
        Set<Id> idd;
        test t = new test();
        idd = t.bfs(g,edgeLabel,source,dir,depth,limit);
//        try (KneighborTraverser traverser = new KneighborTraverser(g)) {
//            ids = traverser.kneighbor(source, dir, edgeLabel,
//                    depth, maxDegree, limit);
//            measure.addIterCount(traverser.vertexIterCounter.get(),
//                    traverser.edgeIterCounter.get());
//        }
//        if (countOnly) {
//            return manager.serializer(g, measure.measures())
//                    .writeMap(ImmutableMap.of("vertices_size", ids.size()));
//        }
        return manager.serializer(g, measure.measures()).writeList("vertices", idd);
    }

    private static class Request {

        @JsonProperty("source")
        public Object source;
        @JsonProperty("steps")
        public TraverserAPI.VESteps steps;
        @JsonProperty("max_depth")
        public int maxDepth;
        @JsonProperty("limit")
        public int limit = Integer.parseInt(DEFAULT_ELEMENTS_LIMIT);
        @JsonProperty("count_only")
        public boolean countOnly = false;
        @JsonProperty("with_vertex")
        public boolean withVertex = false;
        @JsonProperty("with_path")
        public boolean withPath = false;
        @JsonProperty("with_edge")
        public boolean withEdge = false;

        @Override
        public String toString() {
            return String.format("PathRequest{source=%s,steps=%s,maxDepth=%s" +
                            "limit=%s,countOnly=%s,withVertex=%s," +
                            "withPath=%s,withEdge=%s}", this.source, this.steps,
                    this.maxDepth, this.limit, this.countOnly,
                    this.withVertex, this.withPath, this.withEdge);
        }
    }
}
