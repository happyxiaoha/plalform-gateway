package com.dc.appengine.router.nginxparser.service;

import java.util.List;

import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.NgxEntry;

public interface NgxOperate {

	public void deleteMapEntry(NgxBlock conf, String protocol,
			String mapValue, String upstreamName);

	public void addOrupdateMapBlock(NgxBlock conf, String protocol,
			String monitorType, String mapName, String monitorString,
			String upstreamName);

	public void addOrupdateProtocolBlock(NgxBlock conf, String protocol,
			String... entrys);

	public void addOrupdateServerBlock(NgxBlock conf, String protocol,
			String port, String... entrys);

	public void addOrupdateLocationBlock(NgxBlock conf, String protocol,
			String port, String locationValue, String... entrys);


	public void addOrupdateMapUpstreamBlock(NgxBlock conf, String protocol,
			String upstreamValue, boolean isSticky, boolean isOldNgx,
			String monitorType, String mapName, String... entrys);

	public void deleteUpstreamEntry(NgxBlock conf, String protocol,
			String upstreamValue, boolean isOldNgx, String... entrys);

	public void deletelocationBlock(NgxBlock conf, String protocol,
			String port, String locationValue);

	public void deleteServerBlock(NgxBlock conf, String protocol, String port);

	public NgxBlock findServerBlock(NgxBlock conf, String protocol, String port);

	public NgxBlock findProtocolBlock(NgxBlock conf, String protocol);

	public List<NgxEntry> findLocationBlock(NgxBlock conf, String protocol,
			String port);

	public NgxBlock findUpstreamBlock(NgxBlock conf, String protocol,
			String upstreamValue);

	public NgxBlock findMapBolock(NgxBlock conf, String protocol,
			String mapValue);

	void addOrupdateUpstreamBlock(NgxBlock conf, String protocol,
			String upstreamValue, boolean isSticky, boolean isOldNgx,
		 String... entrys);
	public String getUpstreamInMap(NgxBlock conf, String protocol,
			String monitorType, String monitorString, String mapName);
	

}
