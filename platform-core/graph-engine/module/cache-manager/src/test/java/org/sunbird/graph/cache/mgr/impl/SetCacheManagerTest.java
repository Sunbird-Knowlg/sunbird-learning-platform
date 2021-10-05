package org.sunbird.graph.cache.mgr.impl;

import java.util.ArrayList;
import java.util.List;

import org.sunbird.common.exception.ClientException;
import org.junit.Assert;
import org.junit.Test;

public class SetCacheManagerTest {

	@Test
	public void dropSet() {
		List<String> members = new ArrayList<String>();
		members.add("setMember_1");
		members.add("setMember_2");
		SetCacheManager.createSet("domain", "set_94", members);
		List<String> berforeMemberList = SetCacheManager.getSetMembers("domain", "set_94");
		Assert.assertEquals("setMember_1", berforeMemberList.get(0));
		SetCacheManager.dropSet("domain", "set_94");
		List<String> afterMemberList = SetCacheManager.getSetMembers("domain", "set_94");
		Assert.assertTrue(afterMemberList.isEmpty());
	}
	
	@Test
	public void getCardinality() {
		List<String> members = new ArrayList<String>();
		members.add("test");
		members.add("data");
		SetCacheManager.createSet("domain", "set_12390", members);
		Long cardinality = SetCacheManager.getSetCardinality("domain", "set_12390");
		Assert.assertEquals(new Long(2), cardinality);
	}

	@Test
	public void isSetMember_01() {
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_890");
		SetCacheManager.addSetMembers("domain", "set_123", members);
		Boolean isMember = SetCacheManager.isSetMember("domain", "set_123", "do_890");
		Assert.assertEquals(true, isMember);
	}
	
	@Test
	public void isSetMember_02() {
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_890");
		SetCacheManager.addSetMembers("domain", "set_123", members);
		Boolean isMember = SetCacheManager.isSetMember("domain", "set_1234", "do_890");
		Assert.assertEquals(false, isMember);
	}
	
//	@Test(expected = ClientException.class)
//	public void createSetWithoutMembers() {
//		SetCacheManager.createSet("domain", "set_123", null);
//	}
//
//	@Test(expected = ClientException.class)
//	public void createSetWithoutSetId() {
//		List<String> members = new ArrayList<String>();
//		members.add("do_123");
//		SetCacheManager.createSet("domain", null, members);
//	}
//
//	@Test(expected = ClientException.class)
//	public void createSetWithoutGraphId() {
//		List<String> members = new ArrayList<String>();
//		members.add("do_123");
//		SetCacheManager.createSet(null, "set_123", members);
//	}
//
//	@Test(expected = ClientException.class)
//	public void createSetWithoutGraphIdAndSetId() {
//		List<String> members = new ArrayList<String>();
//		members.add("do_123");
//		SetCacheManager.createSet(null, null, members);
//	}
//
//	@Test(expected = ClientException.class)
//	public void createSetWithoutGraphIdAndMembers() {
//		SetCacheManager.createSet(null, "set_123", null);
//	}
//
//	@Test(expected = ClientException.class)
//	public void createSetWithoutMembersAndSetId() {
//		SetCacheManager.createSet("domain", "set_123", null);
//	}
//
//	@Test(expected = ClientException.class)
//	public void createSetWithoutAllParams() {
//		SetCacheManager.createSet(null, null, null);
//	}

	@Test
	public void createSet() {
		List<String> members = new ArrayList<String>();
		members.add("test");
		members.add("data");
		SetCacheManager.createSet("domain", "set_123", members);
		List<String> membersList = SetCacheManager.getSetMembers("domain", "set_123");
		Assert.assertEquals(false, membersList.isEmpty());
		Assert.assertEquals(true, membersList.contains("data"));
	}

	@Test
	public void addSetMember() {
		SetCacheManager.addSetMember("domain", "set_123", "testMember");
		List<String> membersList = SetCacheManager.getSetMembers("domain", "set_123");
		Assert.assertEquals(false, membersList.isEmpty());
		Assert.assertTrue(membersList.contains("testMember"));
	}
	
	@Test
	public void addSetMembers() {
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_890");
		SetCacheManager.addSetMembers("domain", "set_123", members);
		List<String> memberIds = SetCacheManager.getSetMembers("domain", "set_123");
		Assert.assertEquals(true, memberIds.contains("do_1234567"));
		Assert.assertEquals(true, memberIds.contains("do_890"));
	}
	
	@Test
	public void getSetMembers() {
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		SetCacheManager.createSet("domain", "set_90", members);
		List<String> berforeMemberList = SetCacheManager.getSetMembers("domain", "set_90");
		Assert.assertEquals("do_123", berforeMemberList.get(0));
	}

	@Test(expected = ClientException.class)
	public void addSetMemberWithoutgraphId() {
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SetCacheManager.addSetMember(null, "set_123", "do_123456");
	}

	@Test(expected = ClientException.class)
	public void addSetMemberWithoutSetId() {
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SetCacheManager.addSetMember("domain", null, "do_123456");
	}

	@Test(expected = ClientException.class)
	public void addSetMemberWithoutMemberId() {
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SetCacheManager.addSetMember("domain", "set_123", null);
	}

	@Test(expected = ClientException.class)
	public void addSetMemberWithoutMemberIdAndSetId() {
		SetCacheManager.addSetMember("domain", null, null);
	}

	@Test(expected = ClientException.class)
	public void addSetMemberWithoutMemberIdAndGraphId() {
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SetCacheManager.addSetMember(null, "set_123", null);
	}

	@Test(expected = ClientException.class)
	public void addSetMemberWithoutGraphIdAndSetId() {
		SetCacheManager.addSetMember(null, null, "do_123456");
	}
	
	@Test(expected = ClientException.class)
	public void addSetMembersWithoutGraphId() {
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_890");
		SetCacheManager.addSetMembers(null, "set_123", members);
	}

	@Test(expected = ClientException.class)
	public void addSetMembersWithoutSetId() {
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_890");
		SetCacheManager.addSetMembers("domain", null, members);
	}

	@Test(expected = ClientException.class)
	public void addSetMembersWithoutMembersAndGraphId() {
		SetCacheManager.addSetMembers(null, "set_123", null);
	}

	@Test(expected = ClientException.class)
	public void addSetMembersWithoutMembersAndSetId() {
		SetCacheManager.addSetMembers("domain", null, null);
	}

	@Test(expected = ClientException.class)
	public void addSetMembersWithoutGraphIdAndSetId() {
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_890");
		SetCacheManager.addSetMembers(null, null, members);
	}

	@Test(expected = ClientException.class)
	public void addSetMembersWithoutAllParams() {
		SetCacheManager.addSetMembers(null, null, null);
	}

	@Test(expected = ClientException.class)
	public void removeSetMembersWithoutGraphId() {
		SetCacheManager.removeSetMember(null, "set_123", "test");
	}

	@Test(expected = ClientException.class)
	public void removeSetMembersWithoutSetId() {
		SetCacheManager.removeSetMember("domain", null, "test");
	}

	@Test(expected = ClientException.class)
	public void removeSetMembersWithoutMembersAndGraphId() {
		SetCacheManager.removeSetMember("domain", "set_123", null);
	}

	@Test(expected = ClientException.class)
	public void removeSetMembersWithoutMembersAndSetId() {
		SetCacheManager.removeSetMember("domain", null, null);
	}

	@Test(expected = ClientException.class)
	public void removeSetMembersWithoutSetIdAndGraphId() {
		SetCacheManager.removeSetMember(null, null, "do_123455");
	}

	@Test(expected = ClientException.class)
	public void removeSetMembersWithoutAllParams() {
		SetCacheManager.removeSetMember(null, null, null);
	}

	@Test(expected = ClientException.class)
	public void dropSetMembersWithoutGraphId() {
		SetCacheManager.dropSet(null, "set_123");
	}

	@Test(expected = ClientException.class)
	public void dropSetMembersWithoutSetId() {
		SetCacheManager.dropSet("domain", null);
	}

	@Test(expected = ClientException.class)
	public void dropSetMembersWithoutGraphIdAndSetId() {
		SetCacheManager.dropSet(null, null);
	}

	@Test(expected = ClientException.class)
	public void getSetMembersWithoutGraphId() {
		SetCacheManager.getSetMembers(null, "set_123");
	}

	@Test(expected = ClientException.class)
	public void getSetMembersWithoutSetId() {
		SetCacheManager.getSetMembers("domain", null);
	}
	
	@Test(expected = ClientException.class)
	public void getSetMembersWithoutParams() {
		SetCacheManager.getSetMembers(null, null);
	}
	
	@Test(expected = ClientException.class)
	public void getCardinalityWithoutGraphId() {
		SetCacheManager.getSetMembers(null, "set_123");
	}

	@Test(expected = ClientException.class)
	public void getCardinalityWithoutSetId() {
		SetCacheManager.getSetMembers("domain", null);
	}

	@Test(expected = ClientException.class)
	public void getCardinalityWithoutParams() {
		SetCacheManager.getSetMembers(null, null);
	}
}
