function resizeContingence(tileContainer) {
	var t = $("#"+tileContainer);
	var c_width = t.width();
	var c_height = t.height();
	
	var cellSize = t.data("cellSize");
	if (cellSize==null) {
		return;
	}
	console.log("cellSize=",cellSize); 
	
	var spacer = 0;
	var toolbar_height = 50;
	var col_div_height = 100;
	var row_div_width = 150;
	
	var totalcol = t.data("totalcol");
	var totalrow = t.data("totalrow");
	var current_zoom = t.data("current_zoom");
	
	
	var cells_div_width = c_width - row_div_width - spacer - cellSize*current_zoom; // - cellSize car c'est les valeurs marginales
	var cells_div_height = c_height - col_div_height - spacer - toolbar_height - cellSize*current_zoom;

	var cells_div_width = Math.min(totalcol*cellSize*current_zoom,cells_div_width);
	var cells_div_height = Math.min(totalrow*cellSize*current_zoom,cells_div_height);

	$('#'+tileContainer+'-toolbar').parent().width(cells_div_width+spacer+row_div_width);
	$('#'+tileContainer+'-toolbar').parent().height(toolbar_height);

	$('#'+tileContainer+'-columnsName').parent().css({top:(cells_div_height+spacer+cellSize*current_zoom+toolbar_height)});
	$('#'+tileContainer+'-columnsName').parent().width((cells_div_width+spacer+row_div_width));
	$('#'+tileContainer+'-columnsName').parent().height(col_div_height);

	$('#'+tileContainer+'-columnsMarge').parent().css({top:(cells_div_height+spacer+toolbar_height)});
	$('#'+tileContainer+'-columnsMarge').parent().width(cells_div_width);
	$('#'+tileContainer+'-columnsMarge').parent().height(cellSize*current_zoom);

	$('#'+tileContainer+'-cells').parent().css({top:(toolbar_height)});
	$('#'+tileContainer+'-cells').parent().width(cells_div_width);
	$('#'+tileContainer+'-cells').parent().height(cells_div_height);

	$('#'+tileContainer+'-rowsMarge').parent().css({top:toolbar_height,left:(cells_div_width+spacer)});
	$('#'+tileContainer+'-rowsMarge').parent().width(cellSize*current_zoom);
	$('#'+tileContainer+'-rowsMarge').parent().height(cells_div_height);

	$('#'+tileContainer+'-rowsName').parent().css({top:toolbar_height,left:(cells_div_width+spacer+cellSize*current_zoom)});
	$('#'+tileContainer+'-rowsName').parent().width(row_div_width);
	$('#'+tileContainer+'-rowsName').parent().height(cells_div_height);

	//update_tiles
	
		setTimeout(function(){ 	
			var cells_drag = $('#'+tileContainer+'-cells').data("infinitedrag");
			var rows_drag = $('#'+tileContainer+'-rowsName').data("infinitedrag");
			var rowsMarge_drag = $('#'+tileContainer+'-rowsMarge').data("infinitedrag");
			var columnsMarge_drag = $('#'+tileContainer+'-columnsMarge').data("infinitedrag");
			var columns_drag = $('#'+tileContainer+'-columnsName').data("infinitedrag");
			
			cells_drag.update_tiles();
			rows_drag.update_tiles();
			rowsMarge_drag.update_tiles();
			columnsMarge_drag.update_tiles();
			columns_drag.update_tiles();
			
			cells_drag.update_containment();
			rows_drag.update_containment();
			rowsMarge_drag.update_containment();
			columnsMarge_drag.update_containment();
			columns_drag.update_containment();
			
		}, 0.1);
	
	
	/*
	if (cells_drag!=null) {
		setTimeout(function(){ 					
			cells_drag.update_tiles();
			rows_drag.update_tiles();
			rowsMarge_drag.update_tiles();
			columnsMarge_drag.update_tiles();
			columns_drag.update_tiles();
			setTimeout(function(){ 	
				cells_drag.update_containment();
				rows_drag.update_containment();
				rowsMarge_drag.update_containment();
				columnsMarge_drag.update_containment();
				columns_drag.update_containment();
			}, 0.1);
		}, 0.1);
		
	}
	*/
}
function contingenceTile(tileContainer,result,displayConfirmed,zoom,globalSelectedCells) {
		
		var cells_drag = null;
		var rows_drag = null;
		var rowsMarge_drag = null;
		var columnsMarge_drag = null;
		var columns_drag = null;

		var current_zoom = zoom;
		var cellSize = 24; //25;
		var tileSize = cellSize * 20;
		var totalcol = result.modaliteColonne.length;
		var totalrow = result.modaliteLigne.length;

		var c_width = 0;
			var c_height = 0;
			var spacer = 0;
			var toolbar_height = 0;
			var col_div_height = 0;// hauteur du header
			var row_div_width = 0;
			var cells_div_width = 0;
			var cells_div_height  = 0;

		var t = $("#"+tileContainer);
		t.data("current_zoom",zoom);
		t.data("cellSize",cellSize);
		t.data("tileSize",tileSize);
		t.data("totalcol",totalcol);
		t.data("totalrow",totalrow);
		
		
		 
		
		var removeTableau = function (current_zoom) {
			//$(window).off("resize", resizeFunction);
			$("#"+tileContainer+"-dragButton").off("click");
			$("#"+tileContainer+"-clearSelectionButton").off("click");
			$("#"+tileContainer+"-zoomMoinsButton").off("click");
			$("#"+tileContainer+"-zoomPlusButton").off("click");
			$("#"+tileContainer+"-selectionButton").off("click");

			
			cells_drag.remove();
			rows_drag.remove();
			rowsMarge_drag.remove();
			columnsMarge_drag.remove();
			columns_drag.remove();
						
			$("#"+tileContainer).empty();		
			
			contingenceTile(tileContainer,result,displayConfirmed,current_zoom,globalSelectedCells);
			
		};
		
		var divs = $(
				'<div class="divcontingence2" style="overflow:hidden;position:absolute;top:0px;left:0px;">'+
		        '<div id="'+tileContainer+'-toolbar">'+ 
					'<button id="'+tileContainer+'-dragButton" disabled="true" type="button">Drag mode</button>&nbsp; <button id="'+tileContainer+'-selectionButton" type="button">Select Mode</button>&nbsp;<button id="'+tileContainer+'-clearSelectionButton">Clear Selection</button>&nbsp;<button id="'+tileContainer+'-zoomPlusButton">Zoom +</button>&nbsp;<button id="'+tileContainer+'-zoomMoinsButton">Zoom -</button>'+
					'<canvas id="'+tileContainer+'-legend" height="25px" width="125px" style="position:absolute;left:0px;top:24px;background-color:green"></canvas>'+ // 
				'</div>'+
	          '</div>'+
				'<div  class="divcontingence" style="overflow:hidden;position: absolute;left:0px;">'+
				'<div id="'+tileContainer+'-columnsName"></div>'+
			  '</div>'+
				'<div class="divcontingence" style="overflow:hidden;position: absolute;left:0px;">'+
				'<div id="'+tileContainer+'-columnsMarge"></div>'+
			  	'</div>'+
			  '<div class="divcontingence" style="overflow:hidden;position:absolute;left:0px;">'+				 
		        '<div id="'+tileContainer+'-cells"></div>'+
	          '</div>'+
			  '<div class="divcontingence" style="overflow:hidden;position:absolute;">'+
		        '<div id="'+tileContainer+'-rowsMarge"></div>'+
	          '</div>'+				
	          '<div class="divcontingence" style="overflow:hidden;position:absolute;">'+
		        '<div id="'+tileContainer+'-rowsName"></div>'+
	          '</div>'			  
				);

		divs.appendTo($("#"+tileContainer));
		//resizeFunction();
		resizeContingence(tileContainer);
		
		
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
	

	// calcul des tailles max des labels en fonction de la font
	var c_text = $('<canvas></canvas>');
	var ctx_text = $(c_text)[0].getContext("2d");
    ctx_text.scale(current_zoom,current_zoom); 
	var col_max_width = 0;
	var row_max_width = 0;
	ctx_text.font="small-caps 12px sans-serif"; 
	for (var i=0;i<result.modaliteColonne.length;i++) {
		var m = ctx_text.measureText(result.modaliteColonne[i]); // TODO calculer suivant le sin ou cos (car rotation)
		if ((m.width+cellSize)>col_max_width) col_max_width = m.width + cellSize; // on fait +cellSize car c'est la cell des sommes marginales !! -> TODO avoir une autre div (et donc un autre infinite drag)
	}

	ctx_text.font="small-caps 12px sans-serif"; 
	for (var i=0;i<result.modaliteLigne.length;i++) {
		var m = ctx_text.measureText(result.modaliteLigne[i]); 
		if (m.width>row_max_width) row_max_width = m.width;
	}

	if (row_max_width>1000) row_max_width = 1000;
	if (col_max_width>1000) col_max_width = 1000;

	// fin des calcules

	// modaliteColonne : tableau de nom des colonnes
    var max_max_col_height = Math.max(col_max_width*current_zoom,col_div_height);
    
	var columns_tile_options = {			
			start_col: 0,
			start_row: 0,			// TODO : start_height et start_width ou bien start_x, start_y
			max_width:totalcol*cellSize*current_zoom+spacer+row_div_width, 
			max_height:max_max_col_height,
			width: tileSize*current_zoom, 
			height: max_max_col_height,

			oncreate: function($element, col, row) {	
				// Math.max(col_max_width,col_div_height) : pour prendre la plus grande des tailles entre le header et le measureText			
				var canvas = $('<canvas width="'+(tileSize+row_div_width+spacer)+'" height="'+max_max_col_height+'" style="background-color:rgba(0,0,0,0)"></canvas>');
				var ctx = $(canvas)[0].getContext("2d");
				ctx.translate(0.5,0.5)
			    ctx.scale(current_zoom,current_zoom); 
				/* cadre autour du canvas */
				ctx.fillStyle = "black";
				ctx.lineWidth=5;
				ctx.strokeRect(0,0,tileSize,Math.max(col_max_width,col_div_height));
				
				ctx.strokeStyle = "#000000";
				ctx.textBaseline="middle"; 
				ctx.font="small-caps  12px sans-serif"; 
				ctx.fillStyle = "black";
				ctx.lineWidth=0.5;
				for (var x=0;x<tileSize/cellSize;x++) {
					if (x+col*(tileSize/cellSize) >= totalcol) continue;

					var mod = result.modaliteColonne[x+col*(tileSize/cellSize)];
					
					if (mod!=null) {
						ctx.save();						
						ctx.translate(cellSize*x, 0); //col_div_height
						ctx.rotate(0.25*Math.PI);
						/* important : pour avoir un cadre autour du texte
						ctx.fillStyle = "lightgrey";					
						ctx.strokeRect(cellSize/2-1,-cellSize/2,Math.max(col_max_width,col_div_height),cellSize-4); // -8
						*/
						ctx.fillStyle = "black";
						
						ctx.fillText(mod,cellSize/2,0);
						ctx.restore();
					}
				}
				canvas.appendTo($element);

			}
		};	

	var columnsMarge_tile_options = {			
			start_col: 0,
			start_row: 0,			// TODO : start_height et start_width
			max_width:totalcol*cellSize * current_zoom, // on rajoute 1 tileSize pour le texte en biais à la fin 
			max_height:cellSize * current_zoom,
			width: cellSize * current_zoom, 
			height: cellSize * current_zoom,

			oncreate: function($element, col, row) {							
				var canvas = $('<canvas width="'+(tileSize*current_zoom)+'" height="'+(cellSize*current_zoom)+'" style="background-color:rgba(0,0,0,0)"></canvas>');
				var ctx = $(canvas)[0].getContext("2d");
				ctx.scale(current_zoom,current_zoom); 			
				ctx.strokeStyle = "#000000";
				ctx.textBaseline="middle"; 
				ctx.font="bold 10px Arial"; 
				ctx.textAlign="center"; 
				
				ctx.lineWidth=0.5;
				ctx.fillStyle = "lightgrey";
				ctx.strokeRect(2,2,cellSize-4,cellSize-4); // cellule pour les sommes marginales par exemple
				var val = result.margesColonne[col];
				ctx.fillStyle = "white";
				ctx.fillRect(2,2,cellSize-4,cellSize-4);
				ctx.fillStyle = "black";
				ctx.fillText(val,cellSize/2, cellSize/2);
				
				canvas.appendTo($element);

			}
		};	

	

	var max_max_row_width = Math.max(row_max_width * current_zoom,row_div_width);
	var rows_tile_options = {			// 1 tuile par ligne
			start_col: 0,
			start_row: 0,			
			max_width:max_max_row_width, 
			max_height:cellSize * current_zoom * totalrow,
			width: max_max_row_width,
			height: cellSize * current_zoom,

			oncreate: function($element, col, row) {		
				
				var canvas = $('<canvas width="'+max_max_row_width+'" height="'+(cellSize*current_zoom)+'" style="background-color:rgba(0,0,0,0)"></canvas>');
				var ctx = $(canvas)[0].getContext("2d");
				ctx.translate(0.5,0.5);
				ctx.scale(current_zoom,current_zoom); 
				ctx.strokeStyle = "#000000";
				
				ctx.textAlign="left"; 
				ctx.textBaseline="middle"; 
				ctx.font="small-caps  12px sans-serif"; 
				ctx.fillStyle = "black";
				ctx.fillText(result.modaliteLigne[row],0,(cellSize*current_zoom)/2);
		
				canvas.appendTo($element);

			}
		};	
	
	var rowsMarge_tile_options = {			
			start_col: 0,
			start_row: 0,			// TODO : start_height et start_width
			max_width:cellSize * current_zoom, // on rajoute 1 tileSize pour le texte en biais à la fin 
			max_height:cellSize * current_zoom * totalrow,
			width: cellSize * current_zoom, 
			height: cellSize * current_zoom,

			oncreate: function($element, col, row) {							
				var canvas = $('<canvas width="'+(cellSize*current_zoom)+'" height="'+(cellSize*current_zoom)+'" style="background-color:rgba(0,0,0,0)"></canvas>');
				var ctx = $(canvas)[0].getContext("2d");
				ctx.translate(0.5,0.5);
				ctx.scale(current_zoom,current_zoom); 			
				ctx.strokeStyle = "#000000";
				ctx.textBaseline="middle"; 
				ctx.font="bold 10px Arial"; 
				ctx.textAlign="center"; 
				
				ctx.lineWidth=0.5;
				ctx.fillStyle = "lightgrey";
				ctx.strokeRect(2,2,(cellSize-4),(cellSize -4)); // cellule pour les sommes marginales par exemple
				var val = result.margesLigne[row];
				ctx.fillStyle = "white";
				ctx.fillRect(2,2,(cellSize-4),(cellSize-4));
				ctx.fillStyle = "black";
				ctx.fillText(val,(cellSize)/2, (cellSize)/2);
				
				canvas.appendTo($element);

			}
		};	

		 columns_drag = jQuery.infinitedrag("#"+tileContainer+"-columnsName", {  }, columns_tile_options); // axis: "x"
		 columnsMarge_drag = jQuery.infinitedrag("#"+tileContainer+"-columnsMarge", {  }, columnsMarge_tile_options); // axis: "x"
		cells_drag = jQuery.infinitedrag("#"+tileContainer+"-cells", {}, tile_options);
		 rows_drag = jQuery.infinitedrag("#"+tileContainer+"-rowsName", { },rows_tile_options); //axis: "y" 
		 rowsMarge_drag = jQuery.infinitedrag("#"+tileContainer+"-rowsMarge", {  }, rowsMarge_tile_options); // axis: "x"
		
		var isInSelection = false;
		var me = this;
		var selectionCanvas = null;
		var lassoPointToCell = function(lassoPoint) {
			// convert lassoPoint = cell div viewport x,y to cell (col,row) index
			var position = $("#"+tileContainer+"-cells").position();		 // position et non offset (position = relative to its parent)	
			return [Math.floor((lassoPoint[0]-position.left)/(cellSize*current_zoom)),Math.floor((lassoPoint[1]-position.top)/(cellSize*current_zoom))]
		};

		var drawRectCell = function(sctx,firstCell,lastCell) {			
			//sctx.fillStyle="rgba(0, 128, 128, 0.5)";
			//sctx.fillRect(0,0,cells_div_width/current_zoom, cells_div_height/current_zoom);
			sctx.clearRect(0, 0, cells_div_width/current_zoom, cells_div_height/current_zoom);
			sctx.fillStyle="rgba(128, 128, 128, 0.5)";
			var dwidth = Math.abs(firstCell[0] - lastCell[0])+1;
			var dheight = Math.abs(firstCell[1] - lastCell[1])+1;
			
			var minx = Math.min(firstCell[0],lastCell[0]) ;
			var miny = Math.min(firstCell[1],lastCell[1]) ;
			var position = $("#"+tileContainer+"-cells").position();
			sctx.fillRect(minx*(cellSize)+position.left/current_zoom,miny*(cellSize)+position.top/current_zoom,dwidth*cellSize,dheight*cellSize);
		};

		
		var addToSelectedCell = function(firstCell,lastCell) {
			// we need to convert "cells" rect to cells values 
			var selected=[];
			var leftCol = Math.min(firstCell[0],lastCell[0]);
			var rightCol = Math.max(firstCell[0],lastCell[0]);
			var topRow = Math.min(firstCell[1],lastCell[1]);
			var bottomRow = Math.max(firstCell[1],lastCell[1]);
			for (var i=0;i<result.values.length;i++) {	// then for each result (col,row,value) : if in the canvas (defined by col,row of infinite drag) then draw		
				var v = result.values[i];
				if (v.col>=leftCol && v.col<=rightCol && v.row >=topRow && v.row<=bottomRow) {
					selected.push(v);
				}
			}
			//todo : for each selected : highlight cell
			$.each(selected,function (i,v) {
				var canvasCol = Math.floor((v.col*cellSize)/tileSize);
				var canvasRow = Math.floor((v.row*cellSize)/tileSize);
				var can = $("#"+tileContainer+"-cells>div[col='"+canvasCol+"'][row='"+canvasRow+"']>canvas");
				if (can.length == 1) {
					// canvas is found so highlight the cell
					var canvas = can[0];
					var ctx = canvas.getContext("2d");
					//ctx.scale(current_zoom,current_zoom);
					var offsetCol = canvasCol*tileSize/cellSize;		
					var offsetRow = canvasRow*tileSize/cellSize;	
					
					ctx.fillStyle = "#000000";
					ctx.fillRect((v.col-offsetCol)*cellSize+2, (v.row-offsetRow)*cellSize+2, cellSize-4, cellSize-4);
					ctx.fillStyle = "white";
					ctx.fillText(v.value,(v.col-offsetCol)*cellSize+cellSize/2, (v.row-offsetRow)*cellSize+cellSize/2);
					
				}
				
			});
			
			globalSelectedCells = globalSelectedCells.concat(selected);
			// todo : add selected to global selected cells (without duplicate)
			//console.log("selectedcells = ",selected);
		};
	
		var removeSelectedCell = function(cells) { // cells = array de {col,row,value}
			$.each(cells,function (i,v) {
				var canvasCol = Math.floor((v.col*cellSize)/tileSize);
				var canvasRow = Math.floor((v.row*cellSize)/tileSize);
				var can = $("#"+tileContainer+"-cells>div[col='"+canvasCol+"'][row='"+canvasRow+"']>canvas");
				if (can.length == 1) {
					// canvas is found so highlight the cell
					var canvas = can[0];
					var ctx = canvas.getContext("2d");
					
					var offsetCol = canvasCol*tileSize/cellSize;		
					var offsetRow = canvasRow*tileSize/cellSize;	
					
					ctx.fillStyle = colorScale(v.value);
					ctx.fillRect((v.col-offsetCol)*cellSize+2, (v.row-offsetRow)*cellSize+2, cellSize-4, cellSize-4);
					ctx.fillStyle = "white";
					ctx.fillText(v.value,(v.col-offsetCol)*cellSize+cellSize/2, (v.row-offsetRow)*cellSize+cellSize/2);
					
				}
				
			});
		};

		$("#"+tileContainer+"-dragButton").click(function() {
			$("#"+tileContainer+"-dragButton").prop("disabled",true);
			$("#"+tileContainer+"-selectionButton").prop("disabled",false);
			me.isInSelection = false; //! me.isInSelection;
			cells_drag.disabled(me.isInSelection);
			
			if (selectionCanvas!=null) selectionCanvas.remove();
		});

		// gestion de la selection des cellules
		$("#"+tileContainer+"-clearSelectionButton").click(function() {
			removeSelectedCell(globalSelectedCells);
			globalSelectedCells=[];
		});

		$("#"+tileContainer+"-zoomPlusButton").click(function() {
			current_zoom = current_zoom + 0.1;
			$("#"+tileContainer+"-dragButton").click();
			removeTableau(current_zoom);
			//contingenceTile(tileContainer,result,displayConfirmed,current_zoom+ 0.1,globalSelectedCells);
		});

		$("#"+tileContainer+"-zoomMoinsButton").click(function() {
			$("#"+tileContainer+"-dragButton").click();
			current_zoom = current_zoom - 0.1;
			removeTableau(current_zoom);
			//contingenceTile(tileContainer,result,displayConfirmed,current_zoom - 0.1,globalSelectedCells);
		});

		$("#"+tileContainer+"-selectionButton").click(function() {
			$("#"+tileContainer+"-dragButton").prop("disabled",false);
			$("#"+tileContainer+"-selectionButton").prop("disabled",true);
			me.isInSelection = true; //! me.isInSelection;
			cells_drag.disabled(me.isInSelection); // disable the drag for cells div

			if (me.isInSelection) {
				// stop the selection
				// reactivate infinitescroll

				selectionCanvas = $('<canvas id="'+tileContainer+'-selectionArea" width="'+cells_div_width+'" height="'+cells_div_height+'" style="cursor:crosshair;position:absolute;top:'+toolbar_height+'px;left:0px"></canvas>');
				selectionCanvas.appendTo($("#"+tileContainer));
				var sctx = $(selectionCanvas)[0].getContext("2d");
				sctx.scale(current_zoom,current_zoom);
				var firstCell = null; // [col,row]
				var lastCell = null;
				
				selectionCanvas.lasso().bind("lassoDone", function(e, lassoPoints) { // bind fonctionne avec jquery 1.10.X
					  // do something with lassoPoints	
						console.log("selection for cellRect: ",firstCell,lastCell);	
						addToSelectedCell(firstCell,lastCell);				
						firstCell = null;
						lastCell = null;
						sctx.clearRect(0, 0, cells_div_width/current_zoom, cells_div_height/current_zoom);
					}).bind("lassoPoint", function(e, lassoPoint) {
					  // do something with lassoPoint
						
						if (firstCell==null) {
							firstCell = lassoPointToCell(lassoPoint);
							lastCell = firstCell;							
							drawRectCell(sctx,firstCell,firstCell);
							//sctx.beginPath();
							//sctx.moveTo(lassoPoint[0],lassoPoint[1]);
						} else {
							var currentCell = lassoPointToCell(lassoPoint);
							if (currentCell[0]!= lastCell[0] || currentCell[1]!=lastCell[1]) {
								lastCell = currentCell;
								drawRectCell(sctx,firstCell,lastCell);
							}
							//sctx.lineTo(lassoPoint[0],lassoPoint[1]);
							//sctx.stroke();
						}
					});
					
			} else {
				if (selectionCanvas!=null) {
					selectionCanvas.remove();
				};
			}
			
		});


		columns_drag.setCustomDrag(function(ev,ui) {			
			var offset = columns_drag.draggable().offset();						
			
			cells_drag.draggable().offset({left:offset.left});
			columnsMarge_drag.draggable().offset({left:offset.left});

			cells_drag.update_tiles();
			columnsMarge_drag.update_tiles();
			columns_drag.update_tiles();

		});

		columnsMarge_drag.setCustomDrag(function(ev,ui) {			
			var offset = columnsMarge_drag.draggable().offset();			
			
			cells_drag.draggable().offset({left:offset.left});
			columns_drag.draggable().offset({left:offset.left});

			cells_drag.update_tiles();
			columnsMarge_drag.update_tiles();
			columns_drag.update_tiles();

		});

		rows_drag.setCustomDrag(function(ev,ui) {			
			var offset = rows_drag.draggable().offset();						
	
			cells_drag.draggable().offset({top:offset.top});
			rowsMarge_drag.draggable().offset({top:offset.top});
			cells_drag.update_tiles();
			rows_drag.update_tiles();
			rowsMarge_drag.update_tiles();
		});

		rowsMarge_drag.setCustomDrag(function(ev,ui) {			
			var offset = rowsMarge_drag.draggable().offset();			
			
			cells_drag.draggable().offset({top:offset.top});
			rows_drag.draggable().offset({top:offset.top});

			cells_drag.update_tiles();
			rowsMarge_drag.update_tiles();
			rows_drag.update_tiles();

		});

		
		cells_drag.setCustomDrag(function() {
				
				var offset = cells_drag.draggable().offset();				

				columns_drag.draggable().offset({left:offset.left});
				columnsMarge_drag.draggable().offset({left:offset.left});
				rows_drag.draggable().offset({top:offset.top});
				rowsMarge_drag.draggable().offset({top:offset.top});

				cells_drag.update_tiles();
				rows_drag.update_tiles();
				rowsMarge_drag.update_tiles();
				columnsMarge_drag.update_tiles();
				columns_drag.update_tiles();

			});

		
		
		$("#"+tileContainer+"-cells").data("infinitedrag",cells_drag);
		$("#"+tileContainer+"-rowsMarge").data("infinitedrag",rowsMarge_drag);
		$("#"+tileContainer+"-rowsName").data("infinitedrag",rows_drag);
		$("#"+tileContainer+"-columnsMarge").data("infinitedrag",columnsMarge_drag);
		$("#"+tileContainer+"-columnsName").data("infinitedrag",columns_drag);

	} // contingenceTile
