
public class Map {
	
	public static class MyMap<K,V> {
		private Node<K,V>[] nodes; //定义Node 数组保存key，value
		private int size; //逻辑长度
		
		
		//内部静态类Node，用于保存Key，Value 值
		private class Node<K, V> {
			K key;
			V value;
			Node(K key, V value) { //构造函数
				this.key = key;
				this.value = value;
			}
		}
		
		//放入元素，如果元素key 已经存在，覆盖value，否则在数组中添加key，value 信息
		public void put(K key, V value) {
			if(nodes == null) {//如果数组为空，创建大小为10 的数组
				nodes = new Node[10];
			}
			int index = indexOfKey(key); //查找key所在数组位置
			if(index != -1) {
				nodes[index].value = value;
			} else {//如果找不到对应key 值，在数组最后添加node
				nodes[size] = new Node<>(key, value);
				size++;		
			}
		}
		
		//查找key 是否已经存在nodes 中，如果找不到返回-1
		private int indexOfKey(K key) {
			for(int index = 0; index < size; index ++) {
				if(key.equals(this.nodes[index].key)) {
					return index;
				}
			}
			return -1;
		}
		
		//根据key 获取value 值
		public V get(K key) {
			int index = indexOfKey(key);
			if(index != -1) {
				return (V) nodes[index].value;
			}
			return null;
		}
		
		//获取map 长度
		public int size() {
			return size;
		}
	}
	
	public static void main(String[] args) {
		MyMap<String, Person> myMap = new MyMap<>();
		myMap.put("张三", new Person("张三",21));
		myMap.put("李四", new Person("李四",23));
		myMap.put("王五", new Person("王五",22));
		myMap.put("赵六", new Person("赵六",24));
		
		System.out.println("赵六年龄是" + myMap.get("赵六").getAge());
		System.out.println("李四的年龄是：" + myMap.get("李四").getAge());
		System.out.println("myMap.size() " + myMap.size());
	}
}
