app.controller('GameVisualizationController', ['$scope', '$timeout', '$rootScope', '$stateParams', '$state', 'PlayerService', function($scope, $timeout, $rootScope, $stateParams, $state, service) {

    $scope.stats = undefined;
    $timeout(function() {
    	selectLeftMenuTab('forumsTab');
    }, 1000);
    service.getGameCoverage($stateParams.tid).then(function(data) {
        $scope.stats = data.stats;
    	showHeatMap(data);
    });
}]);

function showHeatMap(data) {
    var margin = {
            top: 150,
            right: 10,
            bottom: 70,
            left: 280
        },
        col_number = data.colLabel.length,
        row_number = data.rowLabel.length,
        cellSize = Math.min(parseInt(680/data.colLabel.length),20),
        width = cellSize * col_number, // - margin.left - margin.right,
        height = cellSize * row_number, // - margin.top - margin.bottom,
        legendElementWidth = 150,
        colors = ['#ffffff', '#9ec2b3', '#c684a4'],
    	hcrow = [], // change to gene name or probe id
        hccol = [], // change to gene name or probe id
        rowLabel = data.rowLabel,
        colLabel = data.colLabel;

    _.each(data.rowLabel, function(label, idx) {
    	hcrow.push(idx+1);
    });
    _.each(data.colLabel, function(label, idx) {
    	hccol.push(idx+1);
    });

    var colorScale = d3.scale.quantile()
        .domain([-10, 0, 10])
        .range(colors);

    var svg = d3.select("#chart").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .attr("style", 'float: left')
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    var rowSortOrder = false;
    var colSortOrder = false;
    var rowLabels = svg.append("g")
        .selectAll(".rowLabelg")
        .data(rowLabel)
        .enter()
        .append("text")
        .text(function(d) {
			if(d.length > 40) {
				return (d.substr(0, 37) + '...');
			} else {
            	return d;
			}
        })
        .attr("x", 0)
        .attr("y", function(d, i) {
            return hcrow.indexOf(i + 1) * cellSize;
        })
        .style("text-anchor", "end")
        .attr("transform", "translate(-6," + cellSize / 1.5 + ")")
        .attr("class", function(d, i) {
            return "rowLabel mono r" + i;
        })
        .on("mouseover", function(d) {
            d3.select(this).classed("text-hover", true);
        })
        .on("mouseout", function(d) {
            d3.select(this).classed("text-hover", false);
        })
        .on("click", function(d, i) {
            rowSortOrder = !rowSortOrder;
            sortbylabel("r", i, rowSortOrder);
        });

    var colLabels = svg.append("g")
        .selectAll(".colLabelg")
        .data(colLabel)
        .enter()
        .append("text")
        .text(function(d) {
            if(d.length > 20) {
                return (d.substr(0, 17) + '...');
            } else {
                return d;
            }
        })
        .attr("x", 0)
        .attr("y", function(d, i) {
            return hccol.indexOf(i + 1) * cellSize;
        })
        .style("text-anchor", "left")
        .attr("transform", "translate(" + cellSize / 2 + ",-6) rotate (-90)")
        .attr("class", function(d, i) {
            return "colLabel mono c" + i;
        })
        .on("mouseover", function(d) {
            d3.select(this).classed("text-hover", true);
        })
        .on("mouseout", function(d) {
            d3.select(this).classed("text-hover", false);
        })
        .on("click", function(d, i) {
            colSortOrder = !colSortOrder;
            sortbylabel("c", i, colSortOrder);
        });

    var heatMap = svg.append("g").attr("class", "g3")
        .selectAll(".cellg")
        .data(data.matrix, function(d) {
            return d.row + ":" + d.col;
        })
        .enter()
        .append("rect")
        .attr("x", function(d) {
            return hccol.indexOf(d.col) * cellSize;
        })
        .attr("y", function(d) {
            return hcrow.indexOf(d.row) * cellSize;
        })
        .attr("class", function(d) {
            return "cell cell-border cr" + (d.row - 1) + " cc" + (d.col - 1);
        })
        .attr("width", cellSize)
        .attr("height", cellSize)
        .style("fill", function(d) {
            return colors[d.value];
        })
        .on("mouseover", function(d) {
            //highlight text
            d3.select(this).classed("cell-hover", true);
            d3.selectAll(".rowLabel").classed("text-highlight", function(r, ri) {
                return ri == (d.row - 1);
            });
            d3.selectAll(".colLabel").classed("text-highlight", function(c, ci) {
                return ci == (d.col - 1);
            });

            //Update the tooltip position and value
            d3.select("#tooltip")
                .style("left", (d3.event.pageX + 10 - 220) + "px")
                .style("top", (d3.event.pageY - 10 - 210) + "px")
                .select("#value")
                .html("<b>Concept</b>:" + rowLabel[d.row - 1] + "<br/><b>Game</b>:" + colLabel[d.col - 1] + "<br/><b>" + (d.value == 0 ? 'Concept not covered' : 'Concept Covered') + '</b>');
            d3.select("#tooltip").classed("hidden", false);
        })
        .on("mouseout", function() {
            d3.select(this).classed("cell-hover", false);
            d3.selectAll(".rowLabel").classed("text-highlight", false);
            d3.selectAll(".colLabel").classed("text-highlight", false);
            d3.select("#tooltip").classed("hidden", true);
        });

    var legend = svg.selectAll(".legend")
        .data([0, 1, 2])
        .enter().append("g")
        .attr("class", "legend");

    legend.append("rect")
        .attr("x", function(d, i) {
            return legendElementWidth * i;
        })
        .attr("y", height + (cellSize * 2))
        .attr("width", legendElementWidth)
        .attr("height", cellSize)
        .style("fill", function(d, i) {
            return colors[i];
        });

    legend.append("text")
        .attr("class", "mono")
        .text(function(d) {
            var val = 'No Game/Screener';
            if(d == 1) {
                val = 'Game';
            }
            if(d == 2) {
                val = 'Screener';
            }
            return val;
        })
        .attr("width", legendElementWidth)
        .attr("x", function(d, i) {
            return legendElementWidth * i;
        })
        .attr("y", height + (cellSize * 4));

    // Change ordering of cells

    function sortbylabel(rORc, i, sortOrder) {
        var t = svg.transition().duration(500);
        var log2r = [];
        var sorted; // sorted is zero-based index
        d3.selectAll(".c" + rORc + i)
            .filter(function(ce) {
                log2r.push(ce.value);
            });
        if (rORc == "r") { // sort log2ratio of a gene
            sorted = d3.range(col_number).sort(function(a, b) {
                if (sortOrder) {
                    return log2r[b] - log2r[a];
                } else {
                    return log2r[a] - log2r[b];
                }
            });
            t.selectAll(".cell")
                .attr("x", function(d) {
                    return sorted.indexOf(d.col - 1) * cellSize;
                });
            t.selectAll(".colLabel")
                .attr("y", function(d, i) {
                    return sorted.indexOf(i) * cellSize;
                });
        } else { // sort log2ratio of a contrast
            sorted = d3.range(row_number).sort(function(a, b) {
                if (sortOrder) {
                    return log2r[b] - log2r[a];
                } else {
                    return log2r[a] - log2r[b];
                }
            });
            t.selectAll(".cell")
                .attr("y", function(d) {
                    return sorted.indexOf(d.row - 1) * cellSize;
                });
            t.selectAll(".rowLabel")
                .attr("y", function(d, i) {
                    return sorted.indexOf(i) * cellSize;
                });
        }
    }

    var sa = d3.select(".g3")
        .on("mousedown", function() {
            if (!d3.event.altKey) {
                d3.selectAll(".cell-selected").classed("cell-selected", false);
                d3.selectAll(".rowLabel").classed("text-selected", false);
                d3.selectAll(".colLabel").classed("text-selected", false);
            }
            var p = d3.mouse(this);
            sa.append("rect")
                .attr({
                    rx: 0,
                    ry: 0,
                    class: "selection",
                    x: p[0],
                    y: p[1],
                    width: 1,
                    height: 1
                })
        })
        .on("mousemove", function() {
            var s = sa.select("rect.selection");

            if (!s.empty()) {
                var p = d3.mouse(this),
                    d = {
                        x: parseInt(s.attr("x"), 10),
                        y: parseInt(s.attr("y"), 10),
                        width: parseInt(s.attr("width"), 10),
                        height: parseInt(s.attr("height"), 10)
                    },
                    move = {
                        x: p[0] - d.x,
                        y: p[1] - d.y
                    };

                if (move.x < 1 || (move.x * 2 < d.width)) {
                    d.x = p[0];
                    d.width -= move.x;
                } else {
                    d.width = move.x;
                }

                if (move.y < 1 || (move.y * 2 < d.height)) {
                    d.y = p[1];
                    d.height -= move.y;
                } else {
                    d.height = move.y;
                }
                s.attr(d);

                // deselect all temporary selected state objects
                d3.selectAll('.cell-selection.cell-selected').classed("cell-selected", false);
                d3.selectAll(".text-selection.text-selected").classed("text-selected", false);

                d3.selectAll('.cell').filter(function(cell_d, i) {
                    if (!d3.select(this).classed("cell-selected") &&
                        // inner circle inside selection frame
                        (this.x.baseVal.value) + cellSize >= d.x && (this.x.baseVal.value) <= d.x + d.width &&
                        (this.y.baseVal.value) + cellSize >= d.y && (this.y.baseVal.value) <= d.y + d.height
                    ) {

                        d3.select(this)
                            .classed("cell-selection", true)
                            .classed("cell-selected", true);

                        d3.select(".r" + (cell_d.row - 1))
                            .classed("text-selection", true)
                            .classed("text-selected", true);

                        d3.select(".c" + (cell_d.col - 1))
                            .classed("text-selection", true)
                            .classed("text-selected", true);
                    }
                });
            }
        })
        .on("mouseup", function() {
            // remove selection frame
            sa.selectAll("rect.selection").remove();
            // remove temporary selection marker class
            d3.selectAll('.cell-selection').classed("cell-selection", false);
            d3.selectAll(".text-selection").classed("text-selection", false);
        })
        .on("mouseout", function() {
            if (d3.event.relatedTarget && d3.event.relatedTarget.tagName == 'html') {
                // remove selection frame
                sa.selectAll("rect.selection").remove();
                // remove temporary selection marker class
                d3.selectAll('.cell-selection').classed("cell-selection", false);
                d3.selectAll(".rowLabel").classed("text-selected", false);
                d3.selectAll(".colLabel").classed("text-selected", false);
            }
        });
}
