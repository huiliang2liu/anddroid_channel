package com.channel.json;


public class JSONTest {
	public static void main(String[] args) throws Exception {
		JSONObject jo = new JSONObject();
		jo.put("nihao", false);
		jo.put("shishi", "ʲô���");
		jo.put("xiao",new Xiao());
		System.err.println(jo.toString());
		JSONObject jsonObject=new JSONObject(jo.toString());
		System.out.println(jsonObject.getBoolean("nihao"));
	}

	static class Xiao {
		String name;
		boolean is_man;
		int age;

		public Xiao() {
			// TODO Auto-generated constructor stub
		}
	}
}
