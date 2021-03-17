package org.sunbird.graph.cache.mgr.impl;

import java.util.ArrayList;
import java.util.List;

import org.sunbird.common.exception.ClientException;
import org.junit.Assert;
import org.junit.Test;

public class SequenceCacheMangerTest {

	@Test
	public void createSequence() {
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		members.add("testData");
		members.add("do_234");
		SequenceCacheManager.createSequence("domain", "sequence_123", members);
		List<String> membersList = SequenceCacheManager.getSequenceMembers("domain", "sequence_123");
		Assert.assertEquals(false, membersList.isEmpty());
		Assert.assertEquals("do_123", membersList.get(0));
	}

	@Test
	public void addSequenceMember() {
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SequenceCacheManager.addSequenceMember("domain", "sequence_1236", 12345L, "do_123456");
		List<String> list = SequenceCacheManager.getSequenceMembers("domain", "sequence_1236");
		Assert.assertEquals("do_123456", list.get(0));
	}

	@Test
	public void removeSequenceMembers() {
		List<String> members = new ArrayList<String>();
		members.add("do_1234567");
		members.add("do_89012");
		SequenceCacheManager.addSequenceMember("domain", "sequence_123", 12345L, "do_123456");
		SequenceCacheManager.addSequenceMember("domain", "sequence_123", 12345L, "do_1234567");
		SequenceCacheManager.removeSequenceMember("domain", "sequence_123", "do_123456");
		List<String> memberList = SequenceCacheManager.getSequenceMembers("domain", "sequence_123");
		Assert.assertEquals(true, memberList.contains("do_1234567"));
		Assert.assertEquals(false, memberList.contains("do_123456"));
	}

	@Test
	public void dropSequence() {
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		members.add("data_90");
		SequenceCacheManager.createSequence("domain", "sequence_890", members);
		List<String> berforeMemberList = SequenceCacheManager.getSequenceMembers("domain", "sequence_890");
		Assert.assertEquals(true, berforeMemberList.contains("do_123"));
		SequenceCacheManager.dropSequence("domain", "sequence_890");
		List<String> afterMemberList = SequenceCacheManager.getSequenceMembers("domain", "sequence_890");
		Assert.assertTrue(afterMemberList.isEmpty());
	}

	@Test
	public void getSequenceMembers() {
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		SequenceCacheManager.createSequence("domain", "sequence_89", members);
		List<String> berforeMemberList = SequenceCacheManager.getSequenceMembers("domain", "sequence_89");
		Assert.assertEquals("do_123", berforeMemberList.get(0));
	}

	@Test
	public void getSequenceCardinality() {
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		SequenceCacheManager.createSequence("domain", "sequence_123490", members);
		Long cardinality = SequenceCacheManager.getSequenceCardinality("domain", "sequence_123490");
		Assert.assertEquals(new Long(1),cardinality);
	}

	@Test
	public void isSequenceMember() {
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SequenceCacheManager.addSequenceMember("domain", "sequence_1236", 12345L, "do_123456");
		Boolean isMember = SequenceCacheManager.isSequenceMember("domain", "sequence_1236", "do_123456");
		Assert.assertEquals(true, isMember);
	}

	@Test(expected = ClientException.class)
	public void isSequenceMemberWithoutGraphId() {
		SequenceCacheManager.isSequenceMember(null, "sequence_123", "do_123");
	}

	@Test(expected = ClientException.class)
	public void isSequenceMemberWithoutsequenceId() {
		SequenceCacheManager.isSequenceMember("domain", null, "do_123");
	}

	@Test(expected = ClientException.class)
	public void isSequenceMemberWithoutMemberId() {
		SequenceCacheManager.isSequenceMember("domain", "sequence_123", null);
	}
	
	@Test(expected = ClientException.class)
	public void createSequenceWithoutMembers() {
		SequenceCacheManager.createSequence("domain", "sequence_123", null);
	}

	@Test(expected = ClientException.class)
	public void createSequenceWithoutGraphId() {
		SequenceCacheManager.createSequence(null, "sequence_123", null);
	}

	@Test(expected = ClientException.class)
	public void createSequenceWithoutsequenceId() {
		List<String> members = new ArrayList<String>();
		members.add("do_123");
		SequenceCacheManager.createSequence("domain", null, members);
	}
	
	@Test(expected = ClientException.class)
	public void addSequenceMemberWithoutgraphId() {
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SequenceCacheManager.addSequenceMember(null, "sequence_123", 123L, "do_123456");
	}

	@Test(expected = ClientException.class)
	public void addSequenceMemberWithoutsequenceId() {
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SequenceCacheManager.addSequenceMember("domain", null, 123L, "do_123456");
	}

	@Test(expected = ClientException.class)
	public void addSequenceMemberWithoutMemberId() {
		List<String> members = new ArrayList<String>();
		members.add("do_123456");
		SequenceCacheManager.addSequenceMember("domain", "sequence_123", 123L, null);
	}
	
	@Test(expected = ClientException.class)
	public void removesequenceMembersWithoutGraphId() {
		SequenceCacheManager.removeSequenceMember(null, "sequence_123", "test");
	}

	@Test(expected = ClientException.class)
	public void removesequenceMembersWithoutsequenceId() {
		SequenceCacheManager.removeSequenceMember("domain", null, "test");
	}

	@Test(expected = ClientException.class)
	public void removesequenceMembersWithoutMembers() {
		SequenceCacheManager.removeSequenceMember("domain", "sequence_123", null);
	}
	
	@Test(expected = ClientException.class)
	public void dropSequenceMembersWithoutGraphId() {
		SequenceCacheManager.dropSequence(null, "sequence_123");
	}

	@Test(expected = ClientException.class)
	public void dropSequenceMembersWithoutsequenceId() {
		SequenceCacheManager.dropSequence("domain", null);
	}
	
	@Test(expected = ClientException.class)
	public void getSequenceMembersWithoutGraphId() {
		SequenceCacheManager.getSequenceMembers(null, "sequence_123");
	}

	@Test(expected = ClientException.class)
	public void getSequenceMembersWithoutsequenceId() {
		SequenceCacheManager.getSequenceMembers("domain", null);
	}
	
	@Test(expected = ClientException.class)
	public void getSequenceCardinalityWithoutGraphId() {
		SequenceCacheManager.getSequenceCardinality(null, "sequence_123");
	}

	@Test(expected = ClientException.class)
	public void getSequenceCardinalityWithoutsequenceId() {
		SequenceCacheManager.getSequenceCardinality("domain", null);
	}

}
