
function mergeOptions(divId, config) {
    config = config || {};
    var defaults = {
        viewerWidth: $('#' + divId).width(),
        viewerHeight: $("#" + divId).height(),
        textFontSize: '14px',
        textSelectedFontSize: '15px',
        textSelectedColor: '#428BCA',
        transitionDuration: 1000,
        collapsedNodeFill: 'lightsteelblue',
        expandedNodeFill: '#fff'
    }
    return $.extend({}, defaults, config);
}

// sort the tree according to the node names
function sortTree() {
    tree.sort(function(a, b) {
        return b.name.toLowerCase() < a.name.toLowerCase() ? 1 : -1;
    });
}

// Helper functions for collapsing and expanding nodes.
function collapse(d) {
    if (d.children) {
        d._children = d.children;
        d._children.forEach(collapse);
        d.children = null;
    } else if(d._children){
        d._children.forEach(collapse);
    }
}

function expand(d) {
    if (d._children) {
        d.children = d._children;
        d.children.forEach(expand);
        d._children = null;
    }
}

// Function to center node when clicked/dropped so node doesn't get lost when collapsing/moving with large amount of children.
function centerNode(source) {
    scale = 1;
    x = -source.y0;
    y = -source.x0;
    x = x * scale + 50;
    y = y * scale + options.viewerHeight / 2;
    d3.select('g').transition()
    .duration(options.transitionDuration)
    .attr("transform", "translate(" + x + "," + y + ")scale(" + scale + ")");
}

// Toggle children function
function toggleChildren(d) {
    if (d.children) {
        d._children = d.children;
        d.children = null;
    } else if (d._children) {
        d.children = d._children;
        d._children = null;
    }
    return d;
}

// Toggle children on click.
function click(d) {
    //console.log('d', d);
    if (d3.event.defaultPrevented) return; // click suppressed
    if(d.parent) {
        var children = d.parent.children;
        if(children) {
            children.forEach(function(child) {
                if(child.id != d.id && child.children) {
                    toggleChildren(child);
                }
            });
        }
    }
    if(d.name == 'more') {
        var parent = d.parent;
        var children = parent.children || parent._children;
        var index = d.pageIndex++;
        if(index == d.pages.length) {
            index = 0;
            d.pageIndex = 1;
        }
        var replaceNodes = d.pages[index].nodes;
        if(parent.children) {
            parent.children = null;
            parent.children = replaceChildren(replaceNodes);
            parent.children[parent.children.length] = d;
        } else {
            parent._children = null;
            parent._children = replaceChildren(replaceNodes);
            parent._children[parent._children.length] = d;
        }
    }
    d = toggleChildren(d);
    update(d);
    if(d.name != 'more') {
        if(d.children) {
            centerNode(d);
        } else if(d.parent) {
            centerNode(d.parent);
        } else {
            centerNode(d);
        }
    }
}

function replaceChildren(nodes) {
    var nodeArr = JSON.parse(JSON.stringify(nodes));
    recursiveReplaceChildren(nodeArr);
    return nodeArr;
}

function recursiveReplaceChildren(nodes) {
    nodes.forEach(function(node) {
        node._children = node.children;
        node.children = null;
        if(node._children && node._children.length > 0) {
            recursiveReplaceChildren(node._children);
        }
    });
}

function wordwrap(text, max) {
    var regex = new RegExp(".{0,"+max+"}(?:\\s|$)","g");
    var lines = []

    var line
    while ((line = regex.exec(text))!="") {
        lines.push(line);
    }

    return lines;
}

function update(source) {
    // Compute the new tree layout.
    var nodes = tree.nodes(root).reverse(),
        links = tree.links(nodes);

    var distanceBetweenNodes = 300;
    // Set widths between levels based on maxLabelLength.
    nodes.forEach(function(d) {
        d.y = (d.depth * distanceBetweenNodes);
    });

    // Update the nodes…
    node = svgGroup.selectAll("g.node")
        .data(nodes, function(d) {
            return d.id || (d.id = ++i);
        });

    // Enter any new nodes at the parent's previous position.
    var nodeEnter = node.enter().append("g")
        .attr("class", "node")
        .attr("transform", function(d) {
            return "translate(" + source.y0 + "," + source.x0 + ")";
        });

    nodeEnter.append("circle")
        .attr('class', 'nodeCircle')
        .attr("r", 0)
        .style("fill", function(d) {
            return d._children ? options.collapsedNodeFill : options.expandedNodeFill;
        })
        .on('click', click);

    nodeEnter.append("text")
        .attr("x", '-20')
        .attr("dy", ".35em")
        .attr('class', 'nodeText')
        .style('font-size', options.textFontSize)
        .attr("text-anchor", "start")
        /*.text(function(d) {
            if(d.name == 'more') {
                return "page " + d.pageIndex + '/' + d.pages.length;
            }
            return d.name;
        })*/
        .style("fill-opacity", 1)
        .on("click", function(d) {
            $('.nodeText').attr('font-weight', '');
            $('.nodeText').css({'font-size': options.textFontSize});
            $(this).attr('font-weight', 'bold');
            $(this).css({'font-size': options.textSelectedFontSize});
            ngScope.selectConcept(d);
        })
        .attr("data-title", function(d) {
            return d.name;
        })
        .attr("data-content", function(d) {
            //var html = "<p>The first module is an introduction to software architecture. It establishes the foundation for software architecture thinking. We introduce you to the essential terms and definitions commonly used in software architecture and the role of Quality attributes. We also introduce styles, patterns and tactics that form the basis of designing software architectures.</p>";
            var html = d.description;
            return html;
        })
        .attr("data-toggle", "popover");

    nodeEnter.append("text")
        .attr('text-anchor', "middle")
        .attr("dy", function(d) {
            if(d.name == 'more') {
                return '.30em';
            } else {
                return ".35em";
            }
        })
        .attr('class', 'sumText')
        .style('font-size', function(d) {
            if(d.name == 'more') {
                return '25px';
            } else {
                return "12px";
            }
        })
        .text(function(d) {
            if(d.name == 'more') { return '+'; } else { return (d.concepts || d.subConcepts || d.microConcepts); }
        })
        .on('click', click);

    // Update the text to reflect whether node has children or not.
    node.select('text').attr("x", 20).attr("text-anchor", "start");
    /*node.selectAll('text.nodeText')
    .text(function(d) {
        if(d.name == 'more') {
            return "page " + d.pageIndex + '/' + d.pages.length;
        }
        return d.name;
    });*/

    node.selectAll('text.nodeText')
    .each(function(d) {
        var name = d.name;
        if(d.name == 'more') {
            name = "page " + d.pageIndex + '/' + d.pages.length;
        }
        var lines = wordwrap(name, 50);
        d3.select(this).text('');
        for (var i = 0; i < lines.length; i++) {
            d3.select(this).append("tspan").attr("dy", i==0 ? "0.35em":"1em").attr("x","20").text(lines[i]);
        }
    });

    node.selectAll('text.sumText')
    .text(function(d) {
        if(d.name == 'more') {
            return '+';
        } else {
            return (d.concepts || d.subConcepts || d.microConcepts);
        }
    });

    // Change the circle fill depending on whether it has children and is collapsed
    node.select("circle.nodeCircle").attr("r", 15)
    .style("fill", function(d) {
        return d._children ? options.collapsedNodeFill : options.expandedNodeFill;
    });

    // Transition nodes to their new position.
    var nodeUpdate = node.transition()
        .duration(options.transitionDuration)
        .attr("transform", function(d) {
            return "translate(" + d.y + "," + d.x + ")";
        });

    // Fade the text in
    nodeUpdate.select("text").style("fill-opacity", 1);

    // Transition exiting nodes to the parent's new position.
    var nodeExit = node.exit().transition()
        .duration(options.transitionDuration)
        .attr("transform", function(d) {
            return "translate(" + source.y + "," + source.x + ")";
        })
        .remove();

    nodeExit.select("circle").attr("r", 0);
    nodeExit.select("text").style("fill-opacity", 0);

    // Update the links…
    var link = svgGroup.selectAll("path.link")
        .data(links, function(d) {
            return d.target.id;
        });

    // Enter any new links at the parent's previous position.
    link.enter().insert("path", "g")
    .attr("class", "link")
    .attr("d", function(d) {
        var o = {
            x: source.x0,
            y: source.y0
        };
        return diagonal({
            source: o,
            target: o
        });
    });

    // Transition links to their new position.
    link.transition()
    .duration(options.transitionDuration)
    .attr("d", diagonal);

    // Transition exiting nodes to the parent's new position.
    link.exit().transition()
    .duration(options.transitionDuration)
    .attr("d", function(d) {
        var o = {
            x: source.x,
            y: source.y
        };
        return diagonal({
            source: o,
            target: o
        });
    })
    .remove();

    // Stash the old positions for transition.
    nodes.forEach(function(d) {
        d.x0 = d.x;
        d.y0 = d.y;
    });
}

function selectTextNode(conceptName) {
    var textNodes = svgGroup.selectAll('text.nodeText')[0];
    if(textNodes) {
        for(var i=0; i< textNodes.length; i++) {
            var textNode = textNodes[i];
            if(textNode.textContent == conceptName) {
                var event = document.createEvent("SVGEvents");
                event.initEvent("click",true,true);
                textNode.dispatchEvent(event);
            }
        }
    }
}

function populateParents(node, parentArr) {
    if(node.parent) {
        parentArr.unshift(node.parent);
        populateParents(node.parent, parentArr);
    }
}

function setSelectedNode(nodes, selectedId) {
    for(var i = 0; i < nodes.length; i++) {
        if(selectedId == nodes[i].conceptId) {
            selectedNode = nodes[i];
        } else if(nodes[i].children || nodes[i]._children) {
            setSelectedNode(nodes[i].children || nodes[i]._children, selectedId);
        }
    }
}

function findSelectedNode(nodes) {
    var found = false;
    for(var i = 0; i < nodes.length; i++) {
        if(selectedId == nodes[i].conceptId) {
            found = true;
            break;
        } else if(nodes[i].children || nodes[i]._children) {
            if(findSelectedNode(nodes[i].children || nodes[i]._children)) {
                found = true;
            }
        }
    }
    return found;
}

function setSelectNodeInPagination(nodes, parentPages) {
    if(!nodes) {
        return;
    }
    for(var i = 0; i < nodes.length; i++) {
        if(nodes[i].name == 'more') {
            nodes[i].pages.forEach(function(page) {
                for(var j = 0; j < page.nodes.length; j++) {
                    var found = findSelectedNode([page.nodes[j]], selectedId);
                    if(found) {
                        //console.log('Node found...Swapping', 'parentPages', parentPages);
                        //swap function
                        var swapIndex = 0;
                        if(i == 0) {
                            swapIndex = 1;
                        }
                        var tmpNode = JSON.parse(JSON.stringify(nodes[swapIndex]));
                        nodes[swapIndex] = page.nodes[j];
                        page.nodes[j] = tmpNode;
                    } /*else {
                        parentPages.push({swapNodeTo: nodes[swapIndex], swapNode: page.nodes[j]});
                        setSelectNodeInPagination(page.nodes[j].children || page.nodes[j]._children, JSON.parse(JSON.stringify(parentPages)));
                    }*/
                };
            });
        } else {
            setSelectNodeInPagination(nodes[i].children || nodes[i]._children, JSON.parse(JSON.stringify(parentPages)));
        }
    }
}

// Calculate total nodes, max label length
var totalNodes = 0;
var maxLabelLength = 0;

// Misc. variables
var i = 0;
var root;
var treeLayoutData;
var options;

// define a d3 diagonal projection for use by the node paths later on.
var diagonal = d3.svg.diagonal()
    .projection(function(d) {
        return [d.y, d.x];
    });

// define the baseSvg, attaching a class for styling
 var baseSvg;

// Append a group which holds all nodes
var svgGroup;
var tree;
var selectedId;
var ngScope;

function showDNDTree(data, divId, config, $scope, selectedId) {
    //console.log('Treemap is being generated');
    ngScope = $scope;
    treeLayoutData = JSON.parse(JSON.stringify(data));
    options = mergeOptions(divId, config);
    console.log('options', options);
    // define a d3 diagonal projection for use by the node paths later on.
    var diagonal = d3.svg.diagonal()
        .projection(function(d) {
            return [d.y, d.x];
        });

    // A recursive helper function for performing some setup by walking through all nodes

    function visit(parent, visitFn, childrenFn) {
        if (!parent) return;

        visitFn(parent);

        var children = childrenFn(parent);
        if (children) {
            var count = children.length;
            for (var i = 0; i < count; i++) {
                visit(children[i], visitFn, childrenFn);
            }
        }
    }

    // Call visit function to establish maxLabelLength
    visit(treeLayoutData, function(d) {
        totalNodes++;
        maxLabelLength = Math.max(d.name.length, maxLabelLength);
    }, function(d) {
        return d.children && d.children.length > 0 ? d.children : null;
    });

    // define the baseSvg, attaching a class for styling
    baseSvg = d3.select("#" + divId).append("svg")
        .attr("width", options.viewerWidth)
        .attr("height", options.viewerHeight)
        .attr("class", "overlay");

    // Append a group which holds all nodes
    svgGroup = baseSvg.append("g");

    // Define the root
    root = treeLayoutData;
    root.x0 = 0;
    root.y0 = 0;

    var levelWidth = [1];
    var childCount = function(level, n) {
        if (n.children && n.children.length > 0) {
            if (levelWidth.length <= level + 1) levelWidth.push(0);
            levelWidth[level + 1] += n.children.length;
            n.children.forEach(function(d) {
                childCount(level + 1, d);
            });
        }
    };
    childCount(0, root);
    tree = d3.layout.tree().nodeSize([50, 50]);
    expandANode(selectedId);
}

var selectedNode;
var isInMore;

function expandANode(conceptId, conceptTitle) {
    console.log('Expanding node - ', conceptId, conceptTitle);
    if(selectedId && selectedId == conceptId) {
        if(conceptTitle) {
            selectTextNode(conceptTitle);
        }
        return;
    }
    selectedId = conceptId;
    selectedNode = null;
    isInMore = false;

    if(!selectedId) {
        selectedId = root.conceptId;
    } else {
        var found = findSelectedNode([root]);
        if(!found) {
            setSelectNodeInPagination((root.children || root._children),[]);
        }
    }

    if(root.children) {
        root._children = root.children;
        root.children = null;
    }
    expand(root);
    tree.nodes(root).reverse();
    collapse(root);

    setSelectedNode([root], selectedId);
    //console.log('selectedNode', selectedNode);
    if(selectedNode == null) {
        //console.log('Node not found. Go and search in more levels...');
        root = toggleChildren(root);
        setTimeout(function() {
            update(root);
            centerNode(root);
            selectTextNode(root.name);
        }, 100);
    } else {
        var parentArr = [];
        populateParents(selectedNode, parentArr);
        parentArr.forEach(function(parent) {
            parent = toggleChildren(parent);
            //update(parent);
            //centerNode(parent);
        });
        selectedNode = toggleChildren(selectedNode);
        setTimeout(function() {
            update(selectedNode);
            if(selectedNode.children) {
                centerNode(selectedNode);
            } else if(selectedNode.parent) {
                centerNode(selectedNode.parent);
            } else {
                centerNode(selectedNode);
            }
            selectTextNode(selectedNode.name);
        }, 100);
    }
}