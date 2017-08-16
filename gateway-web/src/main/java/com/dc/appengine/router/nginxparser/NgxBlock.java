/*
 * Copyright 2014 Alexey Plotnik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dc.appengine.router.nginxparser;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Describes block section. Example: http { ... }
 */
public class NgxBlock extends NgxAbstractEntry implements Iterable<NgxEntry> {
	private Collection<NgxEntry> entries = new ArrayList<NgxEntry>();

	public Collection<NgxEntry> getEntries() {
		return entries;
	}

	/**
	 * 将关键字为keyWord的Entry更新为entry
	 * 如keyWord为limit_req_zone，entry为"limit_req_zone $http_dceast_appkey zone=one:20m rate=20r/s;"
	 * @param entry
	 *            最新内容
	 * @param keyWord
	 *            待更新NgxEntry中所包含的关键字
	 */
	public void updateEntry(NgxEntry entry, String keyWord) {
		if (entry instanceof NgxParam) {
			// 如遍历upstream块中所有条目，条目与条目之间用";"隔开
			Iterator<NgxEntry> it = entries.iterator();
			int count = 0;
			while (it.hasNext()) {
				NgxEntry item = it.next();
				if (item instanceof NgxParam) {
					NgxParam p2 = (NgxParam) item;
					if (p2.toString().contains(keyWord)) {
						ArrayList<NgxEntry> tempArrayList = (ArrayList<NgxEntry>) entries;
						// 更新
						tempArrayList.set(count, entry);
					}
				}
				count++;
			}
		}
	}

	
	public void addEntryAtFirst(NgxEntry entry){
		Collection<NgxEntry> tempEnties = new ArrayList<NgxEntry>();
		if (entry instanceof NgxParam) {
			// 如遍历upstream块中所有条目，条目与条目之间用";"隔开
			Iterator<NgxEntry> it = entries.iterator();
			while (it.hasNext()) {
				NgxEntry item = it.next();
				tempEnties.add(item);
				if (item instanceof NgxParam) {
					NgxParam p1 = (NgxParam) entry;
					NgxParam p2 = (NgxParam) item;
					// 待添加条目已存在
					if (p1.toString().equals(p2.toString())){
						return;
					}
				}
			}
		}
		entries.clear();
		entries.add(entry);
		entries.addAll(tempEnties);
	}
	
	
	
	/**
	 * 
	 * 如，可向upstream中的最后加入
	 * "server 127.0.0.2:7003 weight=1 max_fails=3 fail_timeout=30s;"这种字符串
	 * 当upstream中如上字符串已存在，则无法加入！
	 * 
	 * @param entry
	 */
	public void addEntryAtLast(NgxEntry entry) {
		if (entry instanceof NgxParam) {
			// 如遍历upstream块中所有条目，条目与条目之间用";"隔开
			Iterator<NgxEntry> it = entries.iterator();
			while (it.hasNext()) {
				NgxEntry item = it.next();
				if (item instanceof NgxParam) {
					NgxParam p1 = (NgxParam) entry;
					NgxParam p2 = (NgxParam) item;
					// 待添加条目已存在
					if (p1.toString().equals(p2.toString()))
						return;
				}
			}
		}
		// 向(如:upstream)最后附加一个entry
		entries.add(entry);
	}

	@Override
	public String toString() {
		return super.toString() + " {";
	}

	@Override
	public Iterator<NgxEntry> iterator() {
		return getEntries().iterator();
	}

	public void remove(NgxEntry itemToRemove) {
		if (null == itemToRemove)
			throw new NullPointerException("Item can not be null");

		Iterator<NgxEntry> it = entries.iterator();
		while (it.hasNext()) {
			NgxEntry entry = it.next();
			switch (NgxEntryType.fromClass(entry.getClass())) {
			case PARAM:
				if (entry.equals(itemToRemove))
					it.remove();
				break;
			case BLOCK:
				if (entry.equals(itemToRemove))
					it.remove();
				else {
					NgxBlock block = (NgxBlock) entry;
					block.remove(itemToRemove);
				}
				break;
			}
		}
	}

	public void removeAll(Iterable<NgxEntry> itemsToRemove) {
		if (null == itemsToRemove)
			throw new NullPointerException("Items can not be null");
		for (NgxEntry itemToRemove : itemsToRemove) {
			remove(itemToRemove);
		}
	}

	public <T extends NgxEntry> T find(Class<T> clazz, String... params) {
		List<NgxEntry> all = findAll(clazz, new ArrayList<NgxEntry>(), params);
		if (all.isEmpty())
			return null;
		return (T) all.get(0);
	}

	public NgxBlock findBlock(String... params) {
		NgxEntry entry = find(NgxConfig.BLOCK, params);
		if (null == entry)
			return null;
		return (NgxBlock) entry;
	}

	/**
	 * 找某条参数 如http{}中有limit_req_zone $http_dceast_appkey zone=one:20m
	 * rate=20r/s; 传入limit_req_zone将返回limit_req_zone $http_dceast_appkey
	 * zone=one:20m rate=20r/s;这一内容
	 * 
	 * @param params
	 * @return
	 */
	public NgxParam findParam(String... params) {
		NgxEntry entry = find(NgxConfig.PARAM, params);
		if (null == entry)
			return null;
		return (NgxParam) entry;
	}

	/**
	 * @param clazz
	 *            待查找对象类型，比如为NgxConfig.BLOCK
	 * @param params
	 * @return
	 */
	public <T extends NgxEntry> List<NgxEntry> findAll(Class<T> clazz,
			String... params) {
		return findAll(clazz, new ArrayList<NgxEntry>(), params);
	}

	/**
	 * 
	 */
	public <T extends NgxEntry> List<NgxEntry> findAll(Class<T> clazz,
			List<NgxEntry> result, String... params) {
		List<NgxEntry> res = new ArrayList<NgxEntry>();

		if (0 == params.length) {
			return res;
		}
		// params[0]存储的为协议名：为http，stream或tcp
		String head = params[0];
		String[] tail = params.length > 1 ? Arrays.copyOfRange(params, 1,
				params.length) : new String[0];

		for (NgxEntry entry : getEntries()) {
			switch (NgxEntryType.fromClass(entry.getClass())) {
			case PARAM:
				NgxParam param = (NgxParam) entry;
				if (param.getName().equals(head) && param.getClass() == clazz) {
					res.add(param);
				}
				break;

			case BLOCK:
				NgxBlock block = (NgxBlock) entry;
				if (tail.length > 0) {
					if (block.getName().equals(head)) {
						res.addAll(block.findAll(clazz, result, tail));
					}
				} else {
					if (block.getName().equals(head)
							&& (clazz.equals(NgxBlock.class))) {
						res.add(block);
					}
				}
				break;
			}
		}

		return res;
	}
}
