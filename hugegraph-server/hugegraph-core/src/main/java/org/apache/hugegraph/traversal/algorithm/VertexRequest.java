package org.apache.hugegraph.traversal.algorithm;

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

public class VertexRequest {
        private Id vertexId;
        private Directions dir; //in out both
	    private  Id label;
		private int depth;
		private long degree;
		private long limit;



        public VertexRequest(Id vertexId, Directions dir,Id label,int depth,long degree,long limit) {
        	        this.vertexId = vertexId;
        	        this.dir = dir;
					this.label=label;
					this.depth=depth;
					this.degree=degree;
					this.limit=limit;
        	    }

            public Id getVertexId() {
                return vertexId;
            }

            public Directions getEdgeDirection() {
                  return dir;
            }

	public Id getLabel() {
		return label;
	}

	public int getDepth() {
		return depth;
	}

	public long getLimit() {
		return limit;
	}

	public long getDegree() {
		return degree;
	}

	@Override
	    public String toString() {
        	        return "VertexRequest{" +
                	               "vertexId=" + vertexId +
                               ", edgeDirection='" + dir + '\'' +
                	               '}';
        	    }
	}

