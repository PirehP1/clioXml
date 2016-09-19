function resizeContingence(tileContainer) {
	var t = $("#"+tileContainer);
	var config = t.data("config");
	if (config==null) {		
		return;
	}
	var c_width = t.width();
	var c_height = t.height();
	initContingenceSize(tileContainer);
	//update_containment(tileContainer);
	//update_tiles(tileContainer);
	update_containment(tileContainer);
	/*
	setTimeout(function(){ 	
		//update_tiles(tileContainer);
		update_containment(tileContainer);
		
		
	}, 1);
	*/
}

function update_tiles(tileContainer) {
	var t = $("#"+tileContainer);
	
	var drags = t.data("drags");
	if (drags!=null) {		
		drags.cells_drag.update_tiles();
		
	}
	
}

function update_containment(tileContainer) {
	var t = $("#"+tileContainer);
	
	var drags = t.data("drags");
	if (drags!=null) {
		console.log("update_containment !!");
		drags.cells_drag.update_containment();
		drags.cells_drag.update_tiles();
		
	}
	
}
function initContingenceSize(tileContainer) {
	var t = $("#"+tileContainer);
	var t_width = t.width();
	var t_height = t.height();
	
	var config = t.data("config");
	if (config==null) {
		
		return;
	}
	var cells_div_width = t_width ;
	var cells_div_height = t_height - config.toolbar_height ;
	var cells_div_width = Math.min(config.totalcol*config.cellSize*config.current_zoom,cells_div_width);
	var cells_div_height = Math.min(config.totalrow*config.cellSize*config.current_zoom,cells_div_height);
	
	$('#'+tileContainer+'-cells').parent().css({top:(config.toolbar_height)});
	$('#'+tileContainer+'-cells').parent().width(cells_div_width);
	$('#'+tileContainer+'-cells').parent().height(cells_div_height);
	
}
function contingenceTile(tileContainer,result,displayConfirmed,zoom,globalSelectedCells) {
		//alert("contingenceTile");
		var cells_drag = null;
		

		var current_zoom = zoom;
		var cellSize = 24; //25;
		var tileSize = cellSize * 20;
		var totalcol = result.modaliteColonne.length;
		var totalrow = result.modaliteLigne.length;
		var toolbar_height = 50;
		
		var config={
			current_zoom:zoom,
			cellSize:cellSize,
			tileSize:tileSize,
			totalcol:totalcol,
			totalrow:totalrow,
			toolbar_height:toolbar_height
		};
		var t = $("#"+tileContainer);
		t.data("config",config);
		
		
		 
		
		
		var divs = $(
				'<div class="divcontingence2" style="overflow:hidden;position:absolute;top:0px;left:0px;">'+
		        '<div id="'+tileContainer+'-toolbar">'+ 
					'<button id="'+tileContainer+'-dragButton" disabled="true" type="button">Drag mode</button>&nbsp; <button id="'+tileContainer+'-selectionButton" type="button">Select Mode</button>&nbsp;<button id="'+tileContainer+'-clearSelectionButton">Clear Selection</button>&nbsp;<button id="'+tileContainer+'-zoomPlusButton">Zoom +</button>&nbsp;<button id="'+tileContainer+'-zoomMoinsButton">Zoom -</button>'+
					'<canvas id="'+tileContainer+'-legend" height="25px" width="125px" style="position:absolute;left:0px;top:24px;background-color:green"></canvas>'+ // 
				'</div>'+
	          '</div>'+
				
			  '<div class="divcontingence" style="overflow:hidden;position:absolute;background-color:green;left:0px;">'+				 
		        '<div id="'+tileContainer+'-cells"></div>'+
	          '</div>'
			  			  
				);

		divs.appendTo($("#"+tileContainer));
		//resizeFunction();
		initContingenceSize(tileContainer);
		
		//resizeContingence(tileContainer);
		$("#"+tileContainer+"-selectionButton").click(function() {
			//update_tiles(tileContainer);
			resizeContingence(tileContainer);
		});
		
		$("#"+tileContainer+"-clearSelectionButton").click(function() {
			update_containment(tileContainer); 
		});
		
		var min = result.min;
		var max = result.max;
	
		if (min == max) {
			min = 0;
		}
	
	
		
		var colorScale = d3.scale.linear()
		.domain([min, min+(max-min)/2, max])
		.range(["lightgray", "blue", "red"]); // TODO pour éviter de prendre tout D3.js : il faudrait ne prendre que cette partie.

		var legendCtx = $("#"+tileContainer+"-legend")[0].getContext("2d");
		// affichage de la legende
		var cellLegendSize = 25;
		var legend=[min,min+(max-min)*1/4,min+(max-min)/2,min+(max-min)*3/4,max];
		
		
		legendCtx.textBaseline="middle"; 
		legendCtx.font="bold 10px Arial"; 
		legendCtx.textAlign="center"; 
		for (var i=0;i<legend.length;i++) {
			legendCtx.fillStyle = colorScale(legend[i]);
			legendCtx.fillRect(i*cellLegendSize, 0, cellLegendSize, cellLegendSize);
			legendCtx.fillStyle = "white";
			legendCtx.fillText(legend[i],i*cellLegendSize+cellLegendSize/2, cellLegendSize/2);			
		}
			
		

		var tile_options = {
			width: tileSize*current_zoom,
			height: tileSize * current_zoom,
			start_col: 0,
			start_row: 0,
			
			max_width:totalcol*cellSize * current_zoom,
			max_height:totalrow*cellSize * current_zoom,
			onclick:function() {
				console.log("click on cell viewport");
			},
			oncreate: function($element, col, row) {				
				
				var canvas = $('<canvas width="'+(tileSize*current_zoom)+'" height="'+(tileSize * current_zoom)+'" style="background-color:white"></canvas>');
				
				var ctx = $(canvas)[0].getContext("2d");
				ctx.translate(0.5, 0.5);
				ctx.scale(current_zoom,current_zoom); 
				ctx.strokeStyle = "grey";
				ctx.lineWidth=cellSize*2;
				if (row==0) { // draw top border
					ctx.beginPath();
					ctx.moveTo(0,0);
					ctx.lineTo(tileSize,0);
					ctx.stroke();
				}
				var rr = totalrow/(tileSize/cellSize);
				if (row==Math.floor(rr)) { // draw bottom border
					ctx.beginPath();
					//var yyy = totalrow*cellSize - (row-1)*(tileSize/cellSize);
					var yyy = (rr - Math.floor(rr)) * tileSize;
					ctx.moveTo(0,yyy);
					ctx.lineTo(tileSize,yyy);
					ctx.stroke();

				}

				if (col==0) { // draw left border
					ctx.beginPath();
					ctx.moveTo(0,0);
					ctx.lineTo(0,tileSize);
					ctx.stroke();
				}	

				var cc = totalcol/(tileSize/cellSize);
				if (col==Math.floor(cc)) { // draw right border
					ctx.beginPath();					
					var xxx = (cc - Math.floor(cc)) * tileSize;
					ctx.moveTo(xxx,0);
					ctx.lineTo(xxx,tileSize);
					ctx.stroke();

				}
			
				ctx.strokeStyle = "#000000";
				ctx.font="bold 10px Arial"; 
				ctx.textAlign="center"; 
				ctx.textBaseline="middle"; 
				ctx.lineWidth=1;
				for (var x=0;x<tileSize/cellSize;x++) { // first : fill the canvas with all empty cells
					for (var y=0;y<tileSize/cellSize;y++) {
						ctx.fillStyle = "black";
						ctx.strokeRect(x*cellSize+2,y*cellSize+2,cellSize-4,cellSize-4); // Math.floor(cellSize/6) = -4
						ctx.fillStyle = "white";
						ctx.fillRect(x*cellSize+2,y*cellSize+2,cellSize-4,cellSize-4);						
					}				
				}
				
				for (var i=0;i<result.values.length;i++) {	// then for each result (col,row,value) : if in the canvas (defined by col,row of infinite drag) then draw		
					var v = result.values[i];
					var offsetCol = col*tileSize/cellSize;		
					var offsetRow = row*tileSize/cellSize;		
					var nextOffsetCol = (col+1)*tileSize/cellSize;		
					var nextOffsetRow = (row+1)*tileSize/cellSize;		
					if (v.col>=offsetCol && v.col<nextOffsetCol && v.row>=offsetRow && v.row<nextOffsetRow) { // && v.row ...
						ctx.fillStyle = colorScale(v.value);
						ctx.fillRect((v.col-offsetCol)*cellSize+2, (v.row-offsetRow)*cellSize+2, cellSize-4, cellSize-4);
						ctx.fillStyle = "white";
						ctx.fillText(v.value,(v.col-offsetCol)*cellSize+cellSize/2, (v.row-offsetRow)*cellSize+cellSize/2);
					}
				}
				
				/* cadre autour du canvas
				ctx.fillStyle = "black";
				ctx.lineWidth=5;
				ctx.strokeRect(0,0,tileSize,tileSize);
				*/


				// highlight selected cells
				$.each(globalSelectedCells,function (i,v) {
					if (v.col>=offsetCol && v.col<nextOffsetCol && v.row>=offsetRow && v.row<nextOffsetRow) { // && v.row ...
							ctx.fillStyle = "black";
							ctx.fillRect((v.col-offsetCol)*cellSize+2, (v.row-offsetRow)*cellSize+2, cellSize-4, cellSize-4);
							ctx.fillStyle = "white";
							ctx.fillText(v.value,(v.col-offsetCol)*cellSize+cellSize/2, (v.row-offsetRow)*cellSize+cellSize/2);
						}				
				});

				canvas.appendTo($element);

				

			}
		};
	

	
		cells_drag = jQuery.infinitedrag("#"+tileContainer+"-cells", {}, tile_options);
		
		cells_drag.setCustomDrag(function() {
			
			var offset = cells_drag.draggable().offset();				
			/*
			columns_drag.draggable().offset({left:offset.left});
			columnsMarge_drag.draggable().offset({left:offset.left});
			rows_drag.draggable().offset({top:offset.top});
			rowsMarge_drag.draggable().offset({top:offset.top});
			*/
			cells_drag.update_tiles();
			//rows_drag.update_tiles();
			//rowsMarge_drag.update_tiles();
			//columnsMarge_drag.update_tiles();
			//columns_drag.update_tiles();

		});

		$("#"+tileContainer+"-cells").data("infinitedrag",cells_drag);
		
		var drags={
				cells_drag:cells_drag				
			};
			var t = $("#"+tileContainer);
			t.data("drags",drags);
			
	} // contingenceTile
