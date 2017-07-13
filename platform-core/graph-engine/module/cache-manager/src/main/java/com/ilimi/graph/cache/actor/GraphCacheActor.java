package com.ilimi.graph.cache.actor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.cache.mgr.IDefinitionNodeCacheMgr;
import com.ilimi.graph.cache.mgr.ISequenceCacheMgr;
import com.ilimi.graph.cache.mgr.ISetCacheMgr;
import com.ilimi.graph.cache.mgr.ITagCacheMgr;
import com.ilimi.graph.cache.mgr.impl.DefinitionNodeCacheMgrImpl;
import com.ilimi.graph.cache.mgr.impl.SequenceCacheMgrImpl;
import com.ilimi.graph.cache.mgr.impl.SetCacheMgrImpl;
import com.ilimi.graph.cache.mgr.impl.TagCacheMgrImpl;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;

import akka.actor.ActorRef;

public class GraphCacheActor extends BaseGraphManager {

    protected void invokeMethod(Request request, ActorRef parent) {
        String methodName = request.getOperation();
        try {
            Method method = GraphCacheActorPoolMgr.getMethod(GraphCacheManagers.GRAPH_CACHE_MANAGER, methodName);
            if (null == method) {
                throw new ClientException("ERR_GRAPH_INVALID_OPERATION", "Operation '" + methodName + "' not found");
            } else {
                method.invoke(this, request);
            }
        } catch (Exception e) {
            ERROR(e, parent);
        }
    }

    public void createSequence(Request request) {
        ISequenceCacheMgr cacheMgr = new SequenceCacheMgrImpl(this);
        try {
            cacheMgr.createSequence(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void addSequenceMember(Request request) {
        ISequenceCacheMgr cacheMgr = new SequenceCacheMgrImpl(this);
        try {
            Long cardinality = cacheMgr.addSequenceMember(request);
            OK(GraphDACParams.index.name(), cardinality, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void removeSequenceMember(Request request) {
        ISequenceCacheMgr cacheMgr = new SequenceCacheMgrImpl(this);
        try {
            cacheMgr.removeSequenceMember(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void dropSequence(Request request) {
        ISequenceCacheMgr cacheMgr = new SequenceCacheMgrImpl(this);
        try {
            cacheMgr.dropSequence(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void getSequenceMembers(Request request) {
        ISequenceCacheMgr cacheMgr = new SequenceCacheMgrImpl(this);
        try {
            List<String> memberIds = cacheMgr.getSequenceMembers(request);
            OK(GraphDACParams.members.name(), memberIds, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void getSequenceCardinality(Request request) {
        ISequenceCacheMgr cacheMgr = new SequenceCacheMgrImpl(this);
        try {
            Long cardinality = cacheMgr.getSequenceCardinality(request);
            OK(GraphDACParams.cardinality.name(), cardinality, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void isSequenceMember(Request request) {
        ISequenceCacheMgr cacheMgr = new SequenceCacheMgrImpl(this);
        try {
            Boolean isMember = cacheMgr.isSequenceMember(request);
            OK(GraphDACParams.is_member.name(), isMember, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void saveDefinitionNode(Request request) {
        IDefinitionNodeCacheMgr cacheMgr = new DefinitionNodeCacheMgrImpl(this);
        try {
            cacheMgr.saveDefinitionNode(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void getDefinitionNode(Request request) {
        IDefinitionNodeCacheMgr cacheMgr = new DefinitionNodeCacheMgrImpl(this);
        try {
        	Map<String, Object> map = cacheMgr.getDefinitionNode(request);
            OK(GraphDACParams.definition_node.name(), map, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void createSet(Request request) {
        ISetCacheMgr cacheMgr = new SetCacheMgrImpl(this);
        try {
            String setId = (String) request.get(GraphDACParams.set_id.name());
            cacheMgr.createSet(request);
            OK(GraphDACParams.set_id.name(), setId, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void addSetMember(Request request) {
        ISetCacheMgr cacheMgr = new SetCacheMgrImpl(this);
        try {
            cacheMgr.addSetMember(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void addSetMembers(Request request) {
        ISetCacheMgr cacheMgr = new SetCacheMgrImpl(this);
        try {
            cacheMgr.addSetMembers(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void removeSetMember(Request request) {
        ISetCacheMgr cacheMgr = new SetCacheMgrImpl(this);
        try {
            cacheMgr.removeSetMember(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void dropSet(Request request) {
        ISetCacheMgr cacheMgr = new SetCacheMgrImpl(this);
        try {
            cacheMgr.dropSet(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void getSetMembers(Request request) {
        ISetCacheMgr cacheMgr = new SetCacheMgrImpl(this);
        try {
            List<String> memberIds = cacheMgr.getSetMembers(request);
            OK(GraphDACParams.members.name(), memberIds, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void getSetCardinality(Request request) {
        ISetCacheMgr cacheMgr = new SetCacheMgrImpl(this);
        try {
            Long cardinality = cacheMgr.getSetCardinality(request);
            OK(GraphDACParams.cardinality.name(), cardinality, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void isSetMember(Request request) {
        ISetCacheMgr cacheMgr = new SetCacheMgrImpl(this);
        try {
            Boolean isMember = cacheMgr.isSetMember(request);
            OK(GraphDACParams.is_member.name(), isMember, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void createTag(Request request) {
        ITagCacheMgr cacheMgr = new TagCacheMgrImpl(this);
        try {
            cacheMgr.createTag(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void addTagMember(Request request) {
        ITagCacheMgr cacheMgr = new TagCacheMgrImpl(this);
        try {
            cacheMgr.addTagMember(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void addTagMembers(Request request) {
        ITagCacheMgr cacheMgr = new TagCacheMgrImpl(this);
        try {
            cacheMgr.addTagMembers(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void removeTagMember(Request request) {
        ITagCacheMgr cacheMgr = new TagCacheMgrImpl(this);
        try {
            cacheMgr.removeTagMember(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void dropTag(Request request) {
        ITagCacheMgr cacheMgr = new TagCacheMgrImpl(this);
        try {
            cacheMgr.dropTag(request);
            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void getTagMembers(Request request) {
        ITagCacheMgr cacheMgr = new TagCacheMgrImpl(this);
        try {
            List<String> memberIds = cacheMgr.getTagMembers(request);
            OK(GraphDACParams.members.name(), memberIds, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void getTagCardinality(Request request) {
        ITagCacheMgr cacheMgr = new TagCacheMgrImpl(this);
        try {
            Long cardinality = cacheMgr.getCardinality(request);
            OK(GraphDACParams.cardinality.name(), cardinality, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }

    public void isTagMember(Request request) {
        ITagCacheMgr cacheMgr = new TagCacheMgrImpl(this);
        try {
            Boolean isMember = cacheMgr.isTagMember(request);
            OK(GraphDACParams.is_member.name(), isMember, getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }
}
