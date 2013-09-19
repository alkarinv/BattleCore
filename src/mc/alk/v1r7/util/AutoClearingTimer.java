package mc.alk.v1r7.util;

import java.util.List;

import mc.alk.v1r7.util.AutoClearingTimer.LongObject;

import org.apache.commons.lang.mutable.MutableBoolean;



public class AutoClearingTimer<Key> extends Cache<Key, LongObject<Key>>{

	public AutoClearingTimer() {
		super(new CacheSerializer<Key,LongObject<Key>>(){
			@Override
			public LongObject<Key> load(Key key, MutableBoolean dirty, Object... varArgs) {
				return null;
			}
			@Override
			public void save(List<LongObject<Key>> types) {
				/* do nothing */
			}
		});
	}

	public static class LongObject<K> extends mc.alk.v1r7.util.Cache.CacheObject<K,Long>{
		K key;
		Long val;
		public LongObject(K key){
			this.key = key;
			val = System.currentTimeMillis();
		}
		@Override
		public K getKey() {
			return key;
		}
		public Long getValue() {
			return val;
		}
	}

	public boolean withinTime(Key key, Long timeInterval) {
		LongObject<Key> l = this.get(key);
		if (l == null)
			return false;
		Long curTime = System.currentTimeMillis();
		return (curTime - l.getValue()) < timeInterval;
	}

	public void put(Key key){
		this.put(new LongObject<Key>(key));
	}
}
