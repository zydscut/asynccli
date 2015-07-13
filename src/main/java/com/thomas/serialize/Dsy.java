package com.thomas.serialize;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * for district, useless at the moment
 * @author thomas.zheng
 *
 */
public class Dsy {
	List<String> provinces = new ArrayList<String>();
	Map<String, List<String>> cities = new HashMap<String, List<String>>();
	Map<String, List<String>> districts = new HashMap<String, List<String>>();
	
	public Dsy() {
		
		List<String> lines = new ArrayList<String>();
		try {
			InputStream in = new FileInputStream("src/main/java/com/thomas/serialize/dsy.txt");
			lines = IOUtils.readLines(in);
			init(lines);
		}
		catch (IOException ie) {
			ie.printStackTrace();
		}
	};
	
	private void init(List<String> lines) {
		for(String line : lines) {
			if(! StringUtils.isEmpty(line)) {
				String[] lineSpt = line.split(" ");
				if(lineSpt.length == 2) {
					String code = lineSpt[0];
					String descs = lineSpt[1];
					TypeToken<List<String>> token = new TypeToken<List<String>>(){};
					Gson gson = new Gson();
					List<String> descLst = gson.fromJson(descs, token.getType());
					
					if(code.matches("\\b\\d\\b")) {
						//province 
						//0,["安徽省|1","北京市|2","福建省|3","甘肃省|4"...]
						for(String desc : descLst) {
							String[] descSpt = desc.split("|");
							if(desc.length() == 2) {
								String name = descSpt[0];
								provinces.add(name);
							}
						}
					}
					else if (code.matches("\\b\\d_\\d\\b")){
						//city
						//0_4 ["潮州市|34","东莞市|56","佛山市|61","广州市|73"...]
						List<String> pcities = cities.get(code);
						if(pcities == null) {
							pcities = new ArrayList<String>();
						}
						for(String desc : descLst) {
							String[] descSpt = desc.split("|");
							if(desc.length() == 2) {
								String name = descSpt[0];
								pcities.add(name);
							}
						}
						cities.put(code, pcities);
					}
					else if(code.matches("\\b\\d_\\d_\\d\\b")) {
						List<String> cdistricts = districts.get(code);
						if(cdistricts == null) {
							cdistricts = new ArrayList<String>();
						}
						for(String desc : descLst) {
							String[] descSpt = desc.split("|");
							if(desc.length() == 2) {
								String name = descSpt[0];
								cdistricts.add(name);
							}
						}
						districts.put(code, cdistricts);
					}
				}
			}
		}
	}
	
	public List<String> get() {
		return provinces;
	}
	
	public List<String> get(int province) {
		return cities.get("0_" + province);
	}
	
	public List<String> get(int province, int city) {
		return districts.get("0_" + province + "_" + city);
	}
	
	public String get(int province, int city, int section, int district) {
		List<String> cdistricts = districts.get("0_" + province + "_" + city);
		if(cdistricts != null && cdistricts.size() > district) {
			return cdistricts.get(district);
		}
		return StringUtils.EMPTY;
	}
	
	private static Dsy instance;
	
	public static Dsy getInstance() {
		if(instance == null) {
			instance = new Dsy();
		}
		return instance;
	}
}