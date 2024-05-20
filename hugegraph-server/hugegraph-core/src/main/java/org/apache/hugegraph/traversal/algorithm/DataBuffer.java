package org.apache.hugegraph.traversal.algorithm;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
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

public class DataBuffer {

	private final CopyOnWriteArrayList<DataBufferListener> listeners = new CopyOnWriteArrayList<>();
	private DataBlock[] blocks;
	private int blockSize;
	private int work;
	private int numBlocks;

	private int blockindex;
	private int index;
	private AtomicInteger count = new AtomicInteger(0);//原子计数器

	// 构造函数
	public DataBuffer(int totalSize, int blockSize) {
		this.blockSize = blockSize;
		this.numBlocks = (int) Math.ceil((double) totalSize / blockSize);
		this.work = 1;
		this.blocks = new DataBlock[numBlocks];
		this.blockindex = 0;
		this.index = 0;

		for (int i = 0; i < numBlocks; i++) {
			blocks[i] = new DataBlock(blockSize);
		}
	}

	// 获取特定ID的数据块
	public DataBlock getBlock(int blockId) {
		if (blockId >= 0 && blockId < blocks.length) {
			return blocks[blockId];
		}
		return null;
	}
	public int getwork() {
		return this.work;
	}
	public int getNumBlocks(){return numBlocks;}

	public int getBlockSize() {
		return blockSize;
	}

	public void setWork(int val){
		this.work=val;
	}




	public AtomicInteger getCount(){return count;}

	public boolean compareAndCheckZero() {
		return count.get() == 0;
	}

	public void addListener(DataBufferListener listener) {
		listeners.add(listener);
	}

	public void removeListener(DataBufferListener listener) {
		listeners.remove(listener);
	}

	public void add(Edge edge){
		if(blockindex>=0 && blockindex<blocks.length){
			blocks[blockindex].addEdge(index,edge);
			index++;
			if(index>=blockSize){
				index=0;
				blockindex++;
			}
		}

	}

	public void clear(){
		this.blockindex=0;
		this.index=0;
		for(int i=0;i<blocks.length;i++){
			blocks[i].clearsize();
		}

	}

	public int getindex(){
		return index;
	}

	public int getblockindex(){
		return blockindex;
	}


	protected void notifyListeners() {
		DataBufferEvent event = new DataBufferEvent(this);
		for (DataBufferListener listener : listeners) {
			listener.onDataBufferFull(event);
		}
	}




	// 添加数据到Databuffer
	public void addToDatabuffer(int blockId, int index, Edge edge) {
		if (blockId >= 0 && blockId < blocks.length) {
			blocks[blockId].addEdge(index, edge);
		}
	}
}

