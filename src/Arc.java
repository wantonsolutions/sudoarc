import java.util.ArrayList;
import java.util.Iterator;

class Arc {
	public ArrayList<Integer> dom;
	private boolean dirty;

	public Arc (int domainSize){
		dom = new ArrayList<Integer>(domainSize);
		dirty = false;
		for(int i=1;i<=domainSize;i++){
			dom.add(i);
		}
	}

	public void setDirty(boolean dirty){
		this.dirty = dirty;
	}

	public boolean getDirty(){
		return this.dirty;
	}

	public String toString(){
		String arc = "[";
		Iterator itr = dom.iterator();
		while(itr.hasNext()){
			arc = arc + itr.next().toString();
			if(itr.hasNext()){
				arc = arc + " ";
			}
		}
		arc = arc + "]";
		return arc;
	}	
}



