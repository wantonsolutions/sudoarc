import java.util.ArrayList;
import java.util.Iterator;

class Arc {
	public ArrayList<Integer> dom;
	private boolean dirty;
    	public int value;

	public Arc (int value, int domainSize){
        this.value = value;
		dom = new ArrayList<Integer>(domainSize);
		dirty = false;
		for(int i=1;i<=domainSize;i++){
			dom.add(i);
		}
	}

	public Arc (int value, int []domain, int domainSize){
		this.value = value;
		dom = new ArrayList<Integer>(domainSize);
		dirty=false;
		for(int i =0;i<domainSize;i++){
			dom.add(domain[i]);
		}
	}

	public Arc clone(){
		Integer[] d = this.dom.toArray(new Integer[this.dom.size()]);
		int [] di = new int[this.dom.size()];
		for(int i=0;i<this.dom.size();i++){
			di[i] = d[i].intValue();
		}
		Arc cl = new Arc(this.value,di,this.dom.size());
		cl.setDirty(this.getDirty());
		return cl;
	}
	
	public Arc[] split() {
		Arc[] div = new Arc[2];
		if(this.dom.size() <= 1){
			return null;
		} else {
			int []d1 = new int [this.dom.size()/2];
			int []d2 = new int [this.dom.size()/2 + (this.dom.size() %2)];
			for(int i =0;i<this.dom.size();i++){
				if(i<this.dom.size()/2){
					d1[i] = this.dom.get(i).intValue();
				} else {
					d2[i-this.dom.size()/2] = this.dom.get(i).intValue();
				}
			}
			div[0] = new Arc(this.value, d1, this.dom.size()/2);
			div[1] = new Arc(this.value, d2, this.dom.size()/2 + (this.dom.size() %2));
			if(div[0].dom.size() == 1){
				div[0].value = div[0].dom.get(0).intValue();
			}
			if(div[1].dom.size() == 1){
				div[1].value = div[1].dom.get(0).intValue();
			}
			//System.out.println("left " + div[0].toString() + "\tright" + div[1].toString());
		}
		return div;
	}



	public void setDirty(boolean dirty){
		this.dirty = dirty;
	}

	public boolean getDirty(){
		return this.dirty;
	}

	public String toString(){
		String arc = "("+this.value+")[";
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
