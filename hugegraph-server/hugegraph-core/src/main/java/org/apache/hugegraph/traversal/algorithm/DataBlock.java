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

public class DataBlock {
	private Edge[] edges;

	private int size;

	public DataBlock(int blockSize) {
		this.edges = new Edge[blockSize];
		this.size=0;
	}

	// 添加边到数据块
	public void addEdge(int index, Edge edge) {
		if (index >= 0 && index < edges.length) {
			edges[index] = edge;
			size++;
		}
	}

	// 获取数据块中的边
	public Edge[] getEdges() {
		return edges;
	}

	//获取实际数据大小
	public int getSize(){
		return size;
	}

	public void clearsize(){
		this.size=0;
	}
}

