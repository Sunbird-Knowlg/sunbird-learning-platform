package com.ilimi.graph.cache.mgr.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import com.ilimi.common.exception.ClientException;

public class SetCacheManagerTest {
	
	@Test(expected = ClientException.class)
	public void createSetWithoutMembers() {
		SetCacheManager.createSet("domain", "set_123", null);
	}
	
	@Test(expected = ClientException.class)
	public void createSetWithoutGraphId() {
		SetCacheManager.createSet(null, "set_123", null);
	}
	
	@Test(expected = ClientException.class)
	public void createSetWithoutSetId() {
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		SetCacheManager.createSet("domain", null, members);
	}
	
	@Test
	public void createSet(){
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		SetCacheManager.createSet("domain", "set_123", members);
		List<String> membersList = SetCacheManager.getSetMembers("domain", "set_123");
		Assert.assertEquals(false, membersList.isEmpty());
		Assert.assertEquals("do_123", membersList.get(0));
	}
	
	@Test
	public void addSetMember(){
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SetCacheManager.addSetMember("domain", "set_123", "do_123456");
		List<String> membersList = SetCacheManager.getSetMembers("domain", "set_123");
		Assert.assertEquals(false, membersList.isEmpty());
		Assert.assertTrue(membersList.contains("do_123456"));
	}
	
	@Test(expected = ClientException.class)
	public void addSetMemberWithoutgraphId(){
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SetCacheManager.addSetMember(null, "set_123", "do_123456");
	}
	
	@Test(expected = ClientException.class)
	public void addSetMemberWithoutSetId(){
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SetCacheManager.addSetMember("domain", null, "do_123456");
	}
	
	@Test(expected = ClientException.class)
	public void addSetMemberWithoutMemberId(){
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SetCacheManager.addSetMember("domain", "set_123", null);
	}
	
	@Test
	public void addSetMembers(){
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_890");
		SetCacheManager.addSetMembers("domain", "set_123", members);
		List<String> memberIds = SetCacheManager.getSetMembers("domain", "set_123");
		Assert.assertEquals(true, memberIds.contains("do_1234567"));
		Assert.assertEquals(true, memberIds.contains("do_890"));
	}
	
	@Test(expected = ClientException.class)
	public void addSetMembersWithoutGraphId(){
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_890");
		SetCacheManager.addSetMembers(null, "set_123", members);
	}
	
	@Test(expected = ClientException.class)
	public void addSetMembersWithoutSetId(){
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_890");
		SetCacheManager.addSetMembers("domain", null, members);
	}
	
	@Test(expected = ClientException.class)
	public void addSetMembersWithoutMembers(){
		SetCacheManager.addSetMembers("domain", "set_123", null);
	}
	
	public void removeSetMembers(){
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_89012");
		SetCacheManager.removeSetMember("domain", "set_123", "do_89012");
		List<String> memberList = SetCacheManager.getSetMembers("domain", "set_123");
		Assert.assertEquals(true, memberList.contains("do_1234567"));
		Assert.assertEquals(false, memberList.contains("do_89012"));
	}	

	@Test(expected = ClientException.class)
	public void removeSetMembersWithoutGraphId(){
		SetCacheManager.removeSetMember(null, "set_123", "test");
	}
	
	@Test(expected = ClientException.class)
	public void removeSetMembersWithoutSetId(){
		SetCacheManager.removeSetMember("domain", null, "test");
	}
	
	@Test(expected = ClientException.class)
	public void removeSetMembersWithoutMembers(){
		SetCacheManager.removeSetMember("domain", "set_123", null);
	}
	
	@Test
	public void dropSet(){
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		members.add("data_90");
		SetCacheManager.createSet("domain", "set_89", members);
		List<String> berforeMemberList = SetCacheManager.getSetMembers("domain", "set_89");
		Assert.assertEquals("do_123", berforeMemberList.get(0));
		SetCacheManager.dropSet("domain", "set_89");
		List<String> afterMemberList = SetCacheManager.getSetMembers("domain", "set_89");
		Assert.assertEquals(true, afterMemberList.isEmpty());
	}
	
	@Test(expected = ClientException.class)
	public void dropSetMembersWithoutGraphId(){
		SetCacheManager.dropSet(null, "set_123");
	}
	
	@Test(expected = ClientException.class)
	public void dropSetMembersWithoutSetId(){
		SetCacheManager.dropSet("domain", null);
	}
	
	@Test
	public void getSetMembers(){
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		SetCacheManager.createSet("domain", "set_89", members);
		List<String> berforeMemberList = SetCacheManager.getSetMembers("domain", "set_89");
		Assert.assertEquals("do_123", berforeMemberList.get(0));
	}
	
	@Test(expected = ClientException.class)
	public void getSetMembersWithoutGraphId(){
		SetCacheManager.getSetMembers(null, "set_123");
	}
	
	@Test(expected = ClientException.class)
	public void getSetMembersWithoutSetId(){
		SetCacheManager.getSetMembers("domain", null);
	}
	
	@Test
	public void getCardinality(){
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		Long cardinality = SetCacheManager.getSetCardinality("domain", "set_123");
		Assert.assertEquals(false ,  null == cardinality);
	}
	

	@Test(expected = ClientException.class)
	public void getCardinalityWithoutGraphId(){
		SetCacheManager.getSetMembers(null, "set_123");
	}
	
	@Test(expected = ClientException.class)
	public void getCardinalityWithoutSetId(){
		SetCacheManager.getSetMembers("domain", null);
	}
	
	@Test
	public void isSetMember(){
		Boolean isMember = SetCacheManager.isSetMember("domain", "set_123", "do_123");
		Assert.assertEquals(true, isMember);
	}
	
	@Test(expected = ClientException.class)
	public void isSetMemberWithoutGraphId(){	
		SetCacheManager.isSetMember(null, "set_123", "do_123");
	}
	
	@Test(expected = ClientException.class)
	public void isSetMemberWithoutSetId(){
		SetCacheManager.isSetMember("domain", null , "do_123");
	}
	
	@Test(expected = ClientException.class)
	public void isSetMemberWithoutMemberId(){
		SetCacheManager.isSetMember("domain", "set_123", null);
	}
}
