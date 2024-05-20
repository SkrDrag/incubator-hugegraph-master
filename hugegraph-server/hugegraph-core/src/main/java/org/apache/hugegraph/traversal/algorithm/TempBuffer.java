package org.apache.hugegraph.traversal.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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

public class TempBuffer<T> {
	private List<T>[] buffer;

	private Set<Id> neighbors;
	private int capacity; // 初始容量，定义数组的大小

	public TempBuffer(int capacity) {
		this.capacity = capacity;
		this.buffer = (List<T>[]) new List[capacity];
		for (int i = 0; i < capacity; i++) {
			this.buffer[i] = new ArrayList<>();
		}
		this.neighbors = new HashSet<>();
	}

	// 向指定索引的List添加元素
	public void addToBuffer(int index, T element) {
		if (index >= 0 && index < capacity) {
			buffer[index].add(element);
		} else {
			throw new IndexOutOfBoundsException("Index: " + index + ", Capacity: " + capacity);
		}
	}

	public void addId(Id neighbor){
		neighbors.add(neighbor);
	}

	public Set<Id> getNeighbors(){
		return neighbors;
	}




	// 获取指定索引的List
	public List<T> getList(int index) {
		if (index >= 0 && index < capacity) {
			return buffer[index];
		} else {
			throw new IndexOutOfBoundsException("Index: " + index + ", Capacity: " + capacity);
		}
	}

	// 获取整个buffer数组
	public List<T>[] getBuffer() {
		return buffer;
	}
	public void clear() {
		for (List<T> list : buffer) {
			list.clear();
		}
		neighbors.clear();
	}
}

