var marginLeft = 100; // margin for the matrixViewZone
var marginTop = 100;
var cellSize = 25;
function resizeSVG(graph_id) {
/*
					if ($("#"+graph_id+"_chart").length == 0) {
						console.log("not found");
						return;
					}
					*/
				
/*				
				var svgWidth = $("#"+graph_id+"_chart").width();
				var svgHeight = $("#"+graph_id+"_chart").height();
	*/			
				var svgWidth = $("#"+graph_id).width();
				var svgHeight = $("#"+graph_id).height();
				//console.log("svgWidth=",svgWidth);
				//console.log("svgHeight=",svgHeight);
				
				$('#'+graph_id+'_colsClip').attr("width",svgWidth-marginLeft);
				$('#'+graph_id+'_rowsClip').attr("height",svgHeight-marginTop);
				
				$('#'+graph_id+'_matrixClip').attr("width",svgWidth-marginLeft);
				$('#'+graph_id+'_matrixClip').attr("height",svgHeight-marginTop);
			}
			
function contingence(graph_id,result,displayConfirmed) { // #graph est la div contenant le SVG
	
			var rows=result.modaliteLigne,
			cols = result.modaliteColonne,
			data = result.values,
			rowsMarg = result.margesLigne,
			colsMarg = result.margesColonne,
			min = result.min,
			max = result.max;
			
			if (min == max) {
				min = 0;
			}
			
			$("#"+graph_id).empty();
			var confirmed=[];
			
			
			
			
			var resetConfirmed = function () {				
				d3.select("#"+graph_id).selectAll( 'rect.confirmed').classed( "confirmed", false);
				confirmed=[];
				//displayConfirmed(confirmed);
			};
			$('#'+graph_id).data("resetConfirmed",resetConfirmed);
			
			var id;
			
			
			var svg = d3.select("#"+graph_id).append("svg").attr({id:graph_id+"_chart",width:"100%",height:"100%"});
			
			
			var defs = svg.append("defs");
			defs.append("clipPath").attr("id",graph_id+"_colsClipPath").append("rect").attr({id:graph_id+"_colsClip",x:marginLeft,height:marginTop,width:"100%"});
			defs.append("clipPath").attr("id",graph_id+"_matrixClipPath").append("rect").attr({id:graph_id+"_matrixClip",x:marginLeft-3,y:marginTop-3,height:"100%",width:"100%"});
			defs.append("clipPath").attr("id",graph_id+"_rowsClipPath").append("rect").attr({id:graph_id+"_rowsClip",y:marginTop,height:"100%",width:marginLeft});
			
			svg.append("g")			
			.attr("id",graph_id+"_legend");
			
			svg.append("g")
			.attr("style","clip-path: url(#"+graph_id+"_colsClipPath);")
			.append("g")
			.attr("transform","translate("+marginLeft+","+marginTop+")")
			.append("g")
			.attr("id",graph_id+"_cols");
			
			svg.append("g")
			.attr("style","clip-path: url(#"+graph_id+"_rowsClipPath);")
			.append("g")
			.attr("transform","translate("+marginLeft+","+marginTop+")")
			.append("g")
			.attr("id",graph_id+"_rows");
			
			svg.append("g")
			.attr("id",graph_id+"_matrixViewZone")
			.attr("style","clip-path: url(#"+graph_id+"_matrixClipPath);");
			/*
			<svg id="chart" width="100%" height="100%">			
			<defs>
				<clipPath id="colsClipPath">
					<rect id="colsClip" x="110" height="100px" width="100%"/>					
				</clipPath>
				<clipPath id="matrixClipPath">
					<rect id="matrixClip" x="110" y="110" height="100%" width="100%"/>
				</clipPath>
				<clipPath id="rowsClipPath">				
					<rect id="rowsClip" y="110" height="100%" width="100px" />					
				</clipPath>
			</defs>
			
			
			<g style="clip-path: url(#colsClipPath); ">
				<g transform="translate(110,100)" >
					<g id="cols"  />
				</g>
			</g>
			
			<g style="clip-path: url(#rowsClipPath); ">
				<g transform="translate(100,110)" >
					<g id="rows"  />
				</g>
			</g>
			
			
			<g id="matrixViewZone" style="clip-path: url(#matrixClipPath); " >
				
			
			</g>
			
		</svg>
			*/
			var zoom;
			var transBackup;
			resizeSVG(graph_id);			
			var inDragMod = false;
			var colLabels = d3.selectAll("#"+graph_id+"_cols").selectAll(".colLabel")
			  .data(cols)
			  .enter()
			  .append("text")
			  .text(function (d,i) { return d; })
			  .attr("x", 0)
			  .attr("id",function(d,i) {return "col_id_"+i;})
			  .attr("y", function (d, i) { return i * cellSize; })
			  .style("text-anchor", "left")
			  .attr("transform", "translate("+cellSize/2 + ",-26) rotate (-90)")
			  .attr("class",  function (d,i) { return "colLabel mono c"+i;} )
			  .on("click", function(d,i) {
				
				
				if(d3.select(this).classed("selected")) {
					d3.select(this).classed("selected",false);
					
					$("#col_id_"+i).data("dd").destroy();
					$("#col_id_"+i).removeData("dd");
					
				} else {
					d3.select(this).classed("selected",true);
					//add the dd
					var dd2 = new Ext.dd.DragSource("col_id_"+i,
					{
						
						hasOuterHandles:true,
						ddGroup:"modaliteDrop",
						animRepair:false,		
						//dragData: {"text":"titi",records},
						getDragData:function() {
							var cols = $("text.colLabel.selected");
							
							var recs = new Array();
							$.each(cols,function(i,v) {
								//console.log("cols = ",v.innerHTML);
								var m = Ext.create("SimpleModaliteModel",{modalite:v.innerHTML});
								
								recs.push(m);
							})
							
							return {records:recs};
							
						},
						onBeforeDragX: function(data, e){
							console.log("onBeforeDrag");
							inDragMod = true;
								return true;
						},
						/* suppression zoom : onInitDrag et onEndDrag */
						onInitDrag: function(x, y) {
							inDragMod = true;
							transBackup = zoom.translate();
							var  nbcol = $("text.colLabel.selected").length;
							this.proxy.update("<p>"+nbcol+" modalités</p>");
							this.onStartDrag(x, y);
							return true;
						},
						onEndDrag:function() {			
							
							zoom.translate(transBackup);						
							inDragMod = false;
							
						},
						
						view:{grid:{current_path:result.variableColonne,current_path_type:result.variableColonneType}}
						
					});
					$("#col_id_"+i).data("dd",dd2);
					//this.data("dd") = dd2;
				}
			   })
			  .on("mouseover", function(d) {d3.select(this).classed("text-hover",true);})
			  .on("mouseout" , function(d) {d3.select(this).classed("text-hover",false);})	
			  
			  ;
			  
			  var colLabels = d3.selectAll("#"+graph_id+"_cols").selectAll(".colMarge")
			  .data(colsMarg)
			  .enter()
			  .append("text")
			  .text(function (d,i) { return d; })
			  .attr("y", -8)
			  .attr("x", function (d, i) { return i * cellSize+cellSize/2; })
			  .style("text-anchor", "middle")			 
			  .attr("class",  function (d,i) { return "colMarge";} )
			 	  
			  ;
			  
			  
			var rowLabels = d3.selectAll("#"+graph_id+"_rows").selectAll(".rowLabel")
			  .data(rows)
			  .enter()
			  .append("text")
			  .text(function (d,i) { return d; })
			  .attr("id",function(d,i) {return "row_id_"+i;})
			  .attr("x", 0)
			  .attr("y", function (d, i) { return i * cellSize; })
			  .style("text-anchor", "end")
				.attr("transform", "translate(-30," + cellSize / 1.5 + ")")
			 
			  .attr("class",  function (d,i) { return "rowLabel mono c"+i;} )
			  .on("click", function(d,i) {
				
				
				if(d3.select(this).classed("selected")) {
					d3.select(this).classed("selected",false);
					$("#row_id_"+i).data("dd").destroy();
					$("#row_id_"+i).removeData("dd");
				} else {
					d3.select(this).classed("selected",true);
					var dd2 = new Ext.dd.DragSource("row_id_"+i,
					{
						
						hasOuterHandles:true,
						ddGroup:"modaliteDrop",
						animRepair:false,		
						//dragData: {"text":"titi",records},
						getDragData:function() {
							var cols = $("text.rowLabel.selected");
							
							var recs = new Array();
							$.each(cols,function(i,v) {
								//console.log("cols = ",v.innerHTML);
								var m = Ext.create("SimpleModaliteModel",{modalite:v.innerHTML});
								
								recs.push(m);
							})
							
							return {records:recs};
							
						},
						onBeforeDragX: function(data, e){
							console.log("onBeforeDrag");
							inDragMod = true;
								return true;
						},
						onInitDrag: function(x, y) {
							inDragMod = true;
							transBackup = zoom.translate();
							var  nbcol = $("text.rowLabel.selected").length;
							this.proxy.update("<p>"+nbcol+" modalités</p>");
							this.onStartDrag(x, y);
							return true;
						},
						onEndDrag:function() {
							zoom.translate(transBackup);							
							inDragMod = false;
							
						},
						view:{grid:{current_path:result.variableLigne,current_path_type:result.variableLigneType}}
						
					});
					$("#row_id_"+i).data("dd",dd2);
				}
			   })		  
			  ;
			  
			 /*
			$("#"+graph_id+"_legend").find('rect').each(function() {
				
				var dd = Ext.create('Ext.dd.DD', this, 'tablesDDGroup', {
					isTarget: false
				});
				
				// ou bien
				Ext.dd.DragSource(this);
				}) ;
				ou 
				Ext.dd.DragZone
				*/
			/*
			//console.log("tables=",tables);
			$.each(tables, function(el) {
				console.log("",);
				var dd = Ext.create('Ext.dd.DD', el, 'tablesDDGroup', {
					isTarget: false
				});
			});
			  */
			  var colLabels = d3.selectAll("#"+graph_id+"_rows").selectAll(".rowMarge")
			  .data(rowsMarg)
			  .enter()
			  .append("text")
			  .text(function (d,i) { return d; })
			  .attr("x", -6)
			  .attr("y", function (d, i) { return i * cellSize+cellSize/2+4; })
			  .style("text-anchor", "end")			 
			  .attr("class",  function (d,i) { return "rowMarge";} )
			 	  
			  ;
			  
			  
			var colorScale = d3.scale.linear()
			.domain([min, min+(max-min)/2, max])
			.range(["lightgray", "blue", "red"]);
			
			var legend = svg.selectAll(".legend")				  
				  .data([min,min+(max-min)*25/100,min+(max-min)*50/100,min+(max-min)*75/100,min+(max-min)])
				  .enter().append("g")
				  .attr("class", "legend");
			 
			  legend.append("rect")
				.attr("x", function(d, i) { return cellSize * i; })
				.attr("y", 0)
				.attr("width", cellSize)
				.attr("height", cellSize)
				.style("fill", function(d, i) { return colorScale(d); /*colors[i]; */});
			 
			  legend.append("text")
				.attr("class", "value")
				.attr("text-anchor","middle")	
				.attr("pointer-events","none")			
				.text(function(d) { return d; })				
				.attr("x", function(d, i) { return cellSize * i + cellSize/2; })
				.attr("y", cellSize/2+4);
	
			
			
			var inSelect = false;
			 var lasttrans = [];
			 var oldTrans=[0,0];
			function zoomMatrix() {
				
				
				
				
				if (inDragMod) {					
					return false;
				}
				
				if (inSelect) {
					return false;
				}
				
				var trans = d3.event.translate;
				var scale = d3.event.scale;
				
				
				if (d3.event.scale!=1) {
					zoom.scale(1);
					scale=1;
					trans=oldTrans;
					//trans = d3.event.translate;
				} else {
					oldTrans = d3.event.translate;
				}
				
				var p=d3.mouse(this);
				/*
				if (trans[0]<0) {
					trans[0] = 0;
				}
				if (trans[1]<0) {
					trans[1] = 0;
				}
				*/
				
				
				zoom.translate(trans);
				
				p[0]-=marginLeft;
				p[1]-=marginTop;
				//zoom.center(p);
			    
				d3.selectAll("#"+graph_id+"_cols").attr("transform", "translate(" + [trans[0],0] + ")"+ " scale(" + scale + ")");
				d3.selectAll("#"+graph_id+"_rows").attr("transform", "translate(" + [0,trans[1]] + ")"+ " scale(" + scale + ")");
			  		
				//todo : remplacer d3 par svg :
				d3.select("#"+graph_id).select(".matrixcontent").attr("transform", "translate(" + trans + ")"+ " scale(" + scale + ")");			  		  
				d3.select("#"+graph_id).select(".x.axis").attr("transform", "translate(" + trans + ")"+ " scale(" + scale + ")");			  
				d3.select("#"+graph_id).select(".y.axis").attr("transform", "translate(" + trans + ")"+ " scale(" + scale + ")");
				d3.select("#"+graph_id).select(".edge").attr("transform", "translate(" + trans + ")"+ " scale(" + scale + ")");
					
			
			} //zoomMatrix
		
			
			zoom = d3.behavior.zoom().on('zoom', zoomMatrix);
			
			d3.select("#"+graph_id+"_chart") // #chart
			.call(zoom) ;
		    
		
		
			var matrixViewZone = d3.select("#"+graph_id+"_matrixViewZone")			
			.append("g")
			.attr("transform","translate("+marginLeft+","+marginTop+")");
			
			// ligne verticale (colonne)
			matrixViewZone
			.append("g")
			.attr("class","x axis")
			.selectAll("line")
			.data(d3.range(0, (cols.length)*cellSize, cellSize)) 
			.enter().append("line")
			.attr("x1", function(d) { return d; })
			.attr("y1", 0)
			.attr("x2", function(d) { return d; })
			.attr("y2", rows.length*cellSize);
			
			
			// ligne horizontale (ligne)
			matrixViewZone
			.append("g")
			.attr("class","y axis")			
			.selectAll("line")
			.data(d3.range(0, (rows.length)*cellSize, cellSize)) // rows.length +1
			.enter().append("line")
			.attr("y1", function(d) { return d; })
			.attr("x1", 0)
			.attr("y2", function(d) { return d; })
			.attr("x2", cols.length*cellSize);
			
			
			var heatMap = matrixViewZone
			.append("g")
			.attr("class","matrixcontent")			
			.selectAll(".cellg")
			.data(data,function(d){return d.row+":"+d.col;})
			.enter()
			.append("g")			
			.attr("transform", function(d, i) { return "translate("+(d.col * cellSize)+"," + (d.row * cellSize) + ")"; });
			
			
			heatMap.append("rect")			
			.attr("class", function(d){return "cell cell-border cr"+(d.row)+" cc"+(d.col);})
			.attr("width", cellSize)
			.attr("height", cellSize)
			.style("fill", function (d) {return colorScale(d.value);});
			
			heatMap.append("text")			
			.attr("class",   "value" )
			.attr("text-anchor","middle")	
			.attr("pointer-events","none")			
			.attr("dx",cellSize/2)	
			.attr("dy",cellSize/2+4)	
			.text(function(d){return d.value});
			
			
			// bordure de la zone matrix
			var edge = matrixViewZone.append("rect").attr({
				class:"edge",
				x:-2,
				y:-2,
				width:cols.length*cellSize+4,
				height:rows.length*cellSize+4
			});
			
			
			var startRect = null;
			
			function highlightCell(s) {
				// remove the current selected element
				d3.select("#"+graph_id).selectAll( 'rect.selected').classed( "selected", false);
				var sx = parseFloat(s.attr("x")),
					sy = parseFloat(s.attr("y")),
					sw = parseFloat(s.attr("width")),
					sh = parseFloat(s.attr("height"));
					
				var startxCell = Math.floor(sx/cellSize);
				var startyCell = Math.floor(sy/cellSize);
				var endxCell = Math.floor((sx+sw)/cellSize);
				var endyCell = Math.floor((sy+sh)/cellSize);
				
				
				for (var x=startxCell;x<=endxCell;x++) {
					for (var y=startyCell;y<=endyCell;y++) {						
						var el = heatMap.select(".cr"+y+".cc"+x+":not(.confirmed)");
						if (!el.empty()) {
							el.classed("selected","true");
						}
						
					}
				}				
			}
			
			function endSelection() {
				if (!inSelect) {	
						return;
				}	
				d3.event.stopPropagation();					
				// remove selection frame				
				d3.select("#"+graph_id).selectAll("rect.selection").remove();
				
			    // remove temporary selection marker class and confirmes the selected element				
				d3.select("#"+graph_id).selectAll( 'rect.selected').classed( "selected", false).classed("confirmed",true).each(function(d, i) { 
					confirmed.push({row:rows[d.row],col:cols[d.col]});
				}) ;
				
				displayConfirmed(confirmed);				
				inSelect = false;
			}
			
			d3.select("#"+graph_id+"_chart").on( "mouseup", function() {
				endSelection();
			})
			
			d3.select("#"+graph_id).select(".edge").on( "mousedown", function() {								
				if (d3.event.shiftKey) { // http://bl.ocks.org/lgersman/5311083 pour le selection frame
					inSelect = true;
					d3.event.stopPropagation();									
				} else if (d3.event.ctrlKey) {
					d3.event.stopPropagation();	
					var p = d3.mouse( this);
					var sx = p[0];
					var sy = p[1];
					var x = Math.floor(sx/cellSize);
					var y = Math.floor(sy/cellSize);
					
					var el = heatMap.select(".cr"+y+".cc"+x);
					if (!el.empty()) {
						
						if (el.classed("confirmed") == true) {
							el.classed("confirmed",false);
						} else {
							el.classed("confirmed",true);
						}
						
						confirmed = [];
						d3.select("#"+graph_id).selectAll( 'rect.confirmed').each(function(d, i) { 
							confirmed.push({row:rows[d.row],col:cols[d.col]});
						}) ;
						
						displayConfirmed(confirmed);
						
					} 
							
									
				} else {
					return;
				}
		
				var p = d3.mouse( this); // this
			
				d3.select("#"+graph_id).select(".matrixcontent").append( "rect")
				.attr({
					rx      : 6,
					ry      : 6,
					class   : "selection",
					x       : p[0],
					y       : p[1],
					width   : 0,
					height  : 0
				});
				startRect=p;			
			})		
			.on( "mousemove", function() {	
				if (inDragMod) {
					return;
				}
				if (!inSelect) {	// if not in select
					// so search for cell info 
					//$("#currentCol").html("&nbsp;");
					
					var p = d3.mouse(this);
					var x = Math.floor(p[0]/cellSize);
					var y = Math.floor(p[1]/cellSize);
					var s = heatMap.select(".cr"+y+".cc"+x).each(function(d) {
						var r = d.row;
							var c = d.col;
							var count = d.value;								
							
							//console.log("currentcol = ",rows[r],cols[c],count);
					});
					return
				}			
				d3.event.stopPropagation();					
				
				var s = d3.select("#"+graph_id).select("rect.selection");
				if( !s.empty()) {
					var p = d3.mouse( this), //this
						d = {
							x       : parseInt( s.attr( "x"), 10),
							y       : parseInt( s.attr( "y"), 10),
							width   : parseInt( s.attr( "width"), 10),
							height  : parseInt( s.attr( "height"), 10)
						}
					;
					
					if (startRect[0]<p[0]) {
						d.x = startRect[0];
						d.width = p[0] - startRect[0];
					} else {
						d.x = p[0];
						d.width = startRect[0] - p[0];
					}
					
					if (startRect[1]<p[1]) {
						d.y = startRect[1];
						d.height = p[1] - startRect[1];
					} else {
						d.y = p[1];
						d.height = startRect[1] - p[1];
					}
					
					
				   
					s.attr( d);
					
					highlightCell(s);
					
				}										
				
			})		// on mousemove	
			.on( "mouseup", function() {
				endSelection();
			})			
			.on( "mouseout", function() {
				if (!inSelect) {	
					return;
				}	
				d3.event.stopPropagation();	
			});						
			
		}; // function contingence