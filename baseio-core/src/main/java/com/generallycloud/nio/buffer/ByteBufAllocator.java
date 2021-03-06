package com.generallycloud.nio.buffer;

import com.generallycloud.nio.LifeCycle;

//FIXME 考虑加入free链表
//FIXME 关闭内存池要与申请内存方法互斥
public interface ByteBufAllocator extends LifeCycle{

	public abstract void release(ByteBuf buf);

	public abstract ByteBuf allocate(int capacity);
	
	public abstract int getUnitMemorySize();
	
	public abstract void freeMemory();
	
	public abstract int getCapacity();
	
	public abstract boolean isDirect();

}