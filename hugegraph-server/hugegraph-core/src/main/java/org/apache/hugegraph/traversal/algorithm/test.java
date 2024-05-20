package org.apache.hugegraph.traversal.algorithm;


import net.minidev.json.JSONObject;
import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.structure.HugeEdge;
//import org.apache.hugegraph.structure.HugeVertex;
//import org.apache.hugegraph.traversal.optimize.Text;
//import org.apache.hugegraph.util.JsonUtil;
import org.apache.hugegraph.traversal.optimize.QueryHolder;
import org.apache.hugegraph.traversal.optimize.TraversalUtil;
import org.apache.hugegraph.util.E;
import org.apache.hugegraph.util.NumericUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.hugegraph.backend.id.Id;

import static org.apache.hugegraph.traversal.algorithm.HugeTraverser.*;

import java.util.Set;
import java.util.function.Consumer;

import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.backend.id.EdgeId;
import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.structure.HugeEdge;
import org.apache.hugegraph.traversal.algorithm.records.KneighborRecords;
import org.apache.hugegraph.traversal.algorithm.steps.Steps;
import org.apache.hugegraph.type.define.Directions;
import org.apache.hugegraph.util.E;
import org.apache.tinkerpop.gremlin.structure.Edge;

/**
 * Kneighbor
 */
public class test {
    public static final Integer Default_Limit = 400000;

    public static List<Edge> databuffer = new ArrayList<>();
    private Integer cur_length;
    //private final ProjectionManager.ProjectionProperties property;

    public enum searchDir {
        fromSource,
        fromTarget;

        public searchDir getOpposite() {
            return (this == fromSource) ? fromTarget : fromSource;
        }
    }

    private Map<Id, Integer> idMap;
    private Map<Integer, Id> reverseMap;
    private ArrayList<ArrayList<storedEdge>> s_edgeList;
    private ArrayList<ArrayList<storedEdge>> t_edgeList;

//    public EdgeProjection(ProjectionManager.ProjectionProperties properties) {
//        this.property = properties;
//        this.initializeEdgeArray();
//        this.property.setTime();
//    }

    public test(){


    }

//    private void initializeEdgeArray() {
//        this.s_edgeList = new ArrayList<>();
//        this.t_edgeList = new ArrayList<>();
//        this.idMap = new HashMap<>();
//        this.reverseMap = new HashMap<>();
//        addFromDatabase();
//    }

//    protected void addFromDatabase() {
//        this.cur_length = 0;
//
//        if (this.property.edgeLabel.isEmpty()) SearchByLabel(null, null);
//        else {
//            HashSet<Id> Visited = new HashSet<>();
//            this.property.edgeLabel.forEach(label -> SearchByLabel(label, Visited));
//        }
//        if (this.property.graph.tx().isOpen()) {
//            this.property.graph.tx().close();
//        }
//    }
//   public Set<Id> kneighbor(Id sourceV, Directions dir,
//                         String label, int depth, long degree, long limit) {
//       E.checkNotNull(sourceV, "source vertex id");
//       this.checkVertexExist(sourceV, "source vertex");
//       E.checkNotNull(dir, "direction");
//       checkPositive(depth, "k-neighbor max_depth");
//       checkDegree(degree);
//       checkLimit(limit);
//
//
//       Id labelId = this.getEdgeLabelId(label);
//    // Q1:如果不传,则查所有边是在哪实现的... ("" or null)
//    Id labelId = this.getEdgeLabelId(label);
//
//    // 初始化两个的Set, 添加初始顶点
//    Set<Id> latest = newSet(sourceV);
//    Set<Id> all = newSet(sourceV);
//
//    // 按每层遍历
//    while (depth-- > 0) {
//        long remaining = limit == NO_LIMIT ? NO_LIMIT : limit - all.size();
//        // 每次更新latest集合加入相邻顶点(核心)
//        latest = adjacentVertices(latest, dir, labelId, all, degree, remaining);
//        all.addAll(latest);
//
//        if (limit != NO_LIMIT && all.size() >= limit) break; //遍历点数超过上限则跳出
//    }
//    return all;
//    }

//    private Set<Id> adjacentVertices(Set<Id> vertices, Directions dir,Id label,
//                                     Set<Id> excluded, long degree, long limit) {
//        if (limit == 0) return ImmutableSet.of();
//
//        Set<Id> neighbors = newSet();
//        // 依次遍历latest顶点 (比如在第二层, 则遍历第一层的所有点+起点)
//        for (Id source : vertices) {
//            // 拿到从这个点出发的所有边,,时间复杂度至少O(n)?
//            Iterator<Edge> edges = edgesOfVertex(source, dir,label, degree);
//            while (edges.hasNext()) {
//                HugeEdge e = (HugeEdge) edges.next();
//                // 获得每条边指向的顶点ID
//                Id target = e.id().otherVertexId();
//                // 跳过or添加这个点
//                if (excluded != null && excluded.contains(target)) continue;
//                neighbors.add(target);
//
//                if (limit != NO_LIMIT && neighbors.size() >= limit) return neighbors;
//            }
//        }
//        return neighbors;
//    }

//    Iterator<Edge> edgesOfVertex(Id source, Directions dir, Id label, long limit) {
//        Id[] labels = {};
//        if (label != null) labels = new Id[]{label}; //Q2:为何下面不直接传label
//
//        //通过"fromV + 方向 + 边label"查询边, 这里确定是一个ConditionQuery (查的细节在后面.)
//        Query query = GraphTransaction.constructEdgesQuery(source, dir, labels);
//        if (limit != NO_LIMIT) query.limit(limit);
//
//        return this.graph.edges(query);
//    }

    public Set<Id> bfs(HugeGraph g,String label, Id sourceV, Direction dir,int depth, int limit){
        E.checkNotNull(sourceV, "source vertex id");
        E.checkNotNull(dir, "direction");
        checkPositive(depth, "k-neighbor max_depth");
        checkLimit(limit);

        int dep=0;

        Set<Id> visited = new HashSet<>();
        Set<Id> result = new HashSet<>();
        Queue<Id> queue = new ArrayDeque<>();
        queue.add(sourceV);
        visited.add(sourceV);
        result.add(sourceV);

        while(!queue.isEmpty() && dep <= depth){

                int size = queue.size();
                for(int i=0; i < size;i++) {
                    Set<Id> tmp;
                    Id curid = queue.poll();
                    tmp = searchonenode(g, label, curid, dir);
                    for (Id element : tmp) {
                        if (!visited.contains(element)) {
                            visited.add(element);//标记为已访问
                            result.add(element);
                            queue.add(element);//入队列
                        }
                    }
                }


            //filter(databuffer)-->resultbuffer 多线程
            // 一层结束，结果放入任务队列，并返回部分结果
            dep++;
        }
        return result;

    }

    public Set<Id> searchonenode(HugeGraph g,String label, Id vertex, Direction dir){
        int limit = Default_Limit;
        String page = "";
        Set<Id> idvertex = new HashSet<>();
        do {
            GraphTraversal<?, Edge> traversal;
            if (vertex != null) {
                if (label != null) {
                    traversal = g.traversal().V(vertex).toE(dir, label);
                } else {
                    traversal = g.traversal().V(vertex).toE(dir);
                }
            } else {
                continue;
//                if (label != null) {
//                    traversal = g.traversal().E().hasLabel(label);
//                } else {
//                    traversal = g.traversal().E();
//                }
            }
            traversal = traversal.has(QueryHolder.SYSPROP_PAGE, page).limit(limit);
            while (traversal.hasNext()) {
                HugeEdge edge;
                edge = (HugeEdge) traversal.next();
                databuffer.add(edge);

//                if (visited != null) {
//                    if (visited.contains(edge.id())) continue;
//                    else visited.add(edge.id());
//                }

                //Id ownerVertexId = edge.id().ownerVertexId();
                Id otherVertexId = edge.id().otherVertexId();
                idvertex.add(otherVertexId);
//                double weight = weightFilter(edge);

                //insertIntoList(ownerVertexId);
                //insertIntoList(otherVertexId);



//                s_edgeList.get(idMap.get(ownerVertexId))
//                        .add(new storedEdge(weight, idMap.get(otherVertexId)));
//                t_edgeList.get(idMap.get(otherVertexId))
//                        .add(new storedEdge(weight, idMap.get(ownerVertexId)));
            }

            // update page
            page = TraversalUtil.page(traversal);

        } while (page != null);

        return idvertex;


    }

//    private void SearchByLabel(String label, HashSet<Id> visited) {
//
//        int limit = Default_Limit;
//        String page = "";
//        do {
//            GraphTraversal<?, Edge> traversal;
//            if (label != null) {
//                traversal = this.property.graph.traversal().E().hasLabel(label);
//            } else {
//                traversal = this.property.graph.traversal().E();
//            }
//            traversal = traversal.has(QueryHolder.SYSPROP_PAGE, page).limit(limit);
//            while (traversal.hasNext()) {
//                HugeEdge edge;
//                edge = (HugeEdge) traversal.next();
//
//                if (visited != null) {
//                    if (visited.contains(edge.id())) continue;
//                    else visited.add(edge.id());
//                }
//
//                Id ownerVertexId = edge.id().ownerVertexId();
//                Id otherVertexId = edge.id().otherVertexId();
//                double weight = weightFilter(edge);
//
//                insertIntoList(ownerVertexId);
//                insertIntoList(otherVertexId);
//
//
//
//                s_edgeList.get(idMap.get(ownerVertexId))
//                        .add(new storedEdge(weight, idMap.get(otherVertexId)));
//                t_edgeList.get(idMap.get(otherVertexId))
//                        .add(new storedEdge(weight, idMap.get(ownerVertexId)));
//            }
//
//            // update page
//            page = TraversalUtil.page(traversal);
//
//        } while (page != null);
//    }

    private void insertIntoList(Id VertexId) {
        if (!idMap.containsKey(VertexId)) {
            idMap.put(VertexId, this.cur_length);
            reverseMap.put(this.cur_length, VertexId);
            s_edgeList.add(new ArrayList<>());
            t_edgeList.add(new ArrayList<>());
            this.cur_length += 1;
        }
    }


    public Integer idToInteger(Id id) {
        Integer result = idMap.get(id);
        if (result == null) {
            throw new IllegalArgumentException(String.format("No [%s] in the projection!", id.asString()));
        } else {
            return result;
        }
    }

    public Id IntegerToId(int integerId) {
        return reverseMap.get(integerId);
    }

//    private double weightFilter(HugeEdge edge) {
//
//        if (this.property instanceof ProjectionManager.single_projectionProperty) {
//            ProjectionManager.single_projectionProperty property =
//                    (ProjectionManager.single_projectionProperty) this.property;
//            return getOneWeight(edge, property.weight(), property.defaultWeight());
//        } else {
//            ProjectionManager.multi_projectionProperty property =
//                    (ProjectionManager.multi_projectionProperty) this.property;
//
//            HashMap<String, Double> newMap = property.weight().entrySet()
//                    .stream()
//                    .collect(Collectors.toMap(
//                            Map.Entry::getKey,
//                            entry -> getOneWeight(edge, entry.getKey(), entry.getValue()),
//                            (e1, e2) -> e1,
//                            HashMap::new
//                    ));
//            return property.computeWeight(newMap);
//        }
//    }

    private double getOneWeight(HugeEdge edge, String weightName, double defaultWeight) {
        return edge.property(weightName).isPresent()
                ? NumericUtil.convertToNumber(edge.value(weightName)).doubleValue()
                : defaultWeight;
    }


//    public ProjectionManager.ProjectionProperties properties() {
//        return this.property;
//    }


    public int get_length() {
        return idMap.size();
    }

    public List<storedEdge> edgesOfVertex(searchDir search_dir, int v) {
        return search_dir == searchDir.fromSource ? (this.s_edgeList.size() > v ? s_edgeList.get(v) : new ArrayList<>())
                : (this.t_edgeList.size() > v ? t_edgeList.get(v) : new ArrayList<>());
    }

    public String PrintProjection(String pName) {
        JSONObject graph = new JSONObject();
        int nodekeyCounter = 0;
        for (Map.Entry<Id, Integer> entry : this.idMap.entrySet()) {
            JSONObject node = new JSONObject();
            node.put("Id", entry.getKey().asString());
            JSONObject edges = new JSONObject();
            int edgekeyCounter = 0;
            for (storedEdge edge : this.s_edgeList.get(entry.getValue())) {
                edges.put(String.valueOf(edgekeyCounter++), edge.asJson());
            }
            node.put("Edges", edges);
            graph.put(String.valueOf(nodekeyCounter++), node);
        }
        graph.put("pName", pName);
        return graph.toJSONString();
    }


    public class storedEdge {
        private final Double weight;
        private final Integer other;

        public storedEdge(Double weight, Integer other) {
            this.weight = weight;
            this.other = other;
        }

        public Double getWeight() {
            return weight;
        }

        public Integer getOther() {
            return other;
        }

        public String asJson() {
            JSONObject edgeJson = new JSONObject();
            try{
                reverseMap.get(this.other).asString();
            }
            catch (Exception e){
                throw new RuntimeException("cannot change cause: "+ e);
            }
            edgeJson.put("target", reverseMap.get(this.other).asString());
            edgeJson.put("weight", this.weight);
            return edgeJson.toString();
        }

    }


}



