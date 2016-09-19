/*
 * jQuery Infinite Drag
 * Version 0.2
 * Copyright (c) 2010 Ian Li (http://ianli.com)
 * Licensed under the MIT (http://www.opensource.org/licenses/mit-license.php) license.
 *
 * Requires:
 * jQuery	http://jquery.com
 *
 * Reference:
 * http://ianli.com/infinitedrag/ for Usage
 *
 * Versions:
 * 0.2
 * - Fixed problem with IE 8.0
 * 0.1
 * - Initial implementation
 */

$.fn.extend({
	  lasso: function () {
		return this
		  .mousedown(function (e) {
		    // left mouse down switches on "capturing mode"
		    if (e.which === 1 && !$(this).hasClass("lassoRunning")) {
		      $(this).addClass("lassoRunning");
		      $(this).data("lassoPoints", []);
				
		    }
		  })
		  .mouseup(function (e) {
		    // left mouse up ends "capturing mode" + triggers "Done" event
		    if (e.which === 1 && $(this).hasClass("lassoRunning")) {
		      $(this).removeClass("lassoRunning");
		      $(this).trigger("lassoDone", [$(this).data("lassoPoints")]);
				console.log("in mouseup");
		    }
		  })
		  .mousemove(function (e) {			
		    // mouse move captures co-ordinates + triggers "Point" event
		    if ($(this).hasClass("lassoRunning")) {
		      var point = [e.offsetX, e.offsetY];
		      $(this).data("lassoPoints").push(point);
		      $(this).trigger("lassoPoint", [point]);
				
		    }
		  });
	  }
	});

(function($) {
	

	$.dragMomentum = new function () {    
    var howMuch = 200;  // change this for greater or lesser momentum
    var minDrift = 0; // minimum drift after a drag move
    var easeType = 'easeOutExpo'; //'swing';
// demo visuelle des easing : http://api.jqueryui.com/easings/

    
    //  The standard ease types are 'linear' and 'swing'
    //  To use special ease types, you need this plugin:  
    //  jquery.easing.1.3.js  http://gsgd.co.uk/sandbox/jquery/easing/
    //  special ease types:  'linear',  'swing',  'easeInQuad',  
    //  'easeOutQuad',  'easeInOutQuad',  'easeInCubic',  
    //  'easeOutCubic',  'easeInOutCubic',  'easeInQuart', 
    //  'easeOutQuart',  'easeInOutQuart', 'easeInQuint', 
    //  'easeOutQuint',  'easeInOutQuint',  'easeInSine',  
    //  'easeOutSine',  'easeInOutSine',  'easeInExpo',  
    //  'easeOutExpo',  'easeInOutExpo',  'easeInCirc',  
    //  'easeOutCirc',  'easeInOutCirc',  'easeInElastic',
    //  'easeOutElastic',  'easeInOutElastic',  'easeInBack',
    //  'easeOutBack',  'easeInOutBack',  'easeInBounce',
    //    'easeOutBounce',  'easeInOutBounce'
    //  Also see this page for a great display of the easing types.
    //  http://jqueryui.com/demos/effect/#easing
    
    //  No user options below this point.

    var dXa =[0];
    var dYa =[0];
    var dTa =[0];
    
    this.start = function (elemId, Xa, Ya, Ta)  {
          dXa[elemId] = Xa;
        dYa[elemId] = Ya;
        dTa[elemId] = Ta;
        
      }; // END dragmomentum.start()

    this.end = function (elemId, Xb, Yb, Tb,dragObject)  {        
        var Xa = dXa[elemId];
        var Ya = dYa[elemId];
        var Ta = dTa[elemId];
        var Xc = 0;
        var Yc = 0;

        var dDist = Math.sqrt(Math.pow(Xa-Xb, 2) + Math.pow(Ya-Yb, 2));
        var dTime = Tb - Ta;
        var dSpeed = dDist / dTime;
        dSpeed=Math.round(dSpeed*100)/100;

        var distX =  Math.abs(Xa - Xb);
        var distY =  Math.abs(Ya - Yb);

        var dVelX = (minDrift+(Math.round(distX*dSpeed*howMuch / (distX+distY))));
        var dVelY = (minDrift+(Math.round(distY*dSpeed*howMuch / (distX+distY))));

        var position = $('#'+elemId).position();
        var locX = position.left;
        var locY = position.top;
        
        if ( Xa > Xb ){  // we are moving left
            Xc = locX - dVelX;
        } else {  //  we are moving right
            Xc = locX + dVelX;
        }
    
        if ( Ya > Yb ){  // we are moving up
            Yc = (locY - dVelY);
        } else {  // we are moving down
            Yc = (locY + dVelY);
        }
        
        var newLocX = Xc ;
        var newLocY = Yc ;
        
		var contZ = $('#'+elemId).draggable( "option", "containment" );
						// le cont ne prend pas en compte le offet du parent !!
		
		var pOffset = $('#'+elemId).parent().offset();
		//console.log(pOffset);
		var cont = [contZ[0]-pOffset.left,contZ[1]-pOffset.top,contZ[2]-pOffset.left,contZ[3]-pOffset.top]
		if (newLocX < cont[0]) {
			newLocX = cont[0];
		} else if (newLocX>cont[2]) {
			newLocX = cont[2];
		}

		if (newLocY < cont[1]) {
			newLocY = cont[1];
		} else if (newLocY>cont[3]) {
			newLocY = cont[3];
		}
		
		
        $('#'+elemId).animate({ left:newLocX+"px", top:newLocY+"px" }, {duration:700, easing:easeType,step:function(now,tween) {
				var c = dragObject.getCustomDrag();
				
				if (c) {
					c();
				}
			}
		} );

    }; // END  dragmomentum.end()

};  // END dragMomentum

	/**
	 * Function to create InfiniteDrag object.
	 */
	$.infinitedrag = function(draggable, draggable_options, tile_options) {
		return new InfiniteDrag(draggable, draggable_options, tile_options);
	};
	
	$.infinitedrag.VERSION = 0.1;
	
	/**
	 * The InfiniteDrag object.
	 */
	var InfiniteDrag = function(draggable, draggable_options, tile_options) {
		// Use self to reduce confusion about this.
		var self = this;

		var $draggable = $(draggable);
		var $viewport = $draggable.parent();
		$draggable.css({
			position: "relative",
			cursor: "move"
			
		});
		
		// Draggable options
		var _do = (draggable_options) ? draggable_options : {custom_drag:null};

		// Tile options (DEFAULT)
		var _to = {
			class_name: "_tile",
			width: 100,
			height: 100,
			start_col: 0,
			start_row: 0,
			range_col: [-1000000, 1000000],
			range_row: [-1000000, 1000000],
			max_width:null,
			max_height:null,
			  
			oncreate: function($element, i, j) {
				$element.text(i + "," + j);
			}
		};
		this._to = _to;
		// Override tile options.
		for (var i in tile_options) {
			if (tile_options[i] !== undefined) {
				_to[i] = tile_options[i];
			}
		}
		
		// Override tile options based on draggable options.
		if (_do.axis == "x") {
			_to.range_row = [_to.start_row, _to.start_row];
		} else if (_do.axis == "y") {
			_to.range_col = [_to.start_col, _to.start_col];
		}
		
		if (_to.max_height!=null) {
			_to.range_row = [0, Math.floor(_to.max_height/_to.height)-1];
		}

		if (_to.max_width!=null) {
			_to.range_col = [0, Math.floor(_to.max_width/_to.width)-1];			
		}

		// Creates the tile at (i, j).
		function create_tile(i, j) {
			if (i < _to.range_col[0] || _to.range_col[1] < i) {
				return;
			} else if (j < _to.range_row[0] || _to.range_row[1] < j) {
				return;
			}
			
			grid[i][j] = true;
			var x = i * _to.width;
			var y = j * _to.height;
			var $e = $draggable.append('<div></div>');

			var $new_tile = $e.children(":last");
			$new_tile.attr({
				"class": _to.class_name,
				col: i,
				row: j
			}).css({
				position: "absolute",
				left: x,
				top: y,
				width: _to.width,
				height: _to.height
			});

			_to.oncreate($new_tile, i, j);
		};
		
		// Updates the containment box wherein the draggable can be dragged.
		var update_containment = function() {
			// Update viewport info.
			viewport_width = $viewport.width(),
			viewport_height = $viewport.height(),
			viewport_cols = Math.ceil(viewport_width / _to.width),
			viewport_rows = Math.ceil(viewport_height / _to.height);
			
			// Create containment box.
			var half_width = _to.width / 2,
				half_height = _to.height / 2,
				viewport_offset = $viewport.offset(),
				viewport_draggable_width = viewport_width - _to.width,
				viewport_draggable_height = viewport_height - _to.height;

			var minx,maxx,miny,maxy;
			
			if (_to.max_width!=null) {
				minx = 0;
				maxx = - _to.max_width + _to.width;
			} else {
				minx = -_to.range_col[0] * _to.width;
				maxx = -_to.range_col[1] * _to.width;
			}

			if (_to.max_height!=null) {
				miny = 0;
				maxy = - _to.max_height + _to.height;
			} else {
				miny = -_to.range_row[0] * _to.height
				maxy = -_to.range_row[1] * _to.height
			}

			var containment = [
				(maxx) + viewport_offset.left + viewport_draggable_width,
				(maxy) + viewport_offset.top + viewport_draggable_height,
				(minx) + viewport_offset.left,
				(miny) + viewport_offset.top,
			];
			
			$draggable.draggable("option", "containment", containment);
		};
		
		var update_tiles = function() {
			
			var $this = $draggable;
			var $parent = $this.parent();

			// Problem with .position() in Chrome/WebKit:
			// 		var pos = $(this).position();
			// So, we compute it ourselves.
			var pos = {
				left: $this.offset().left - $parent.offset().left,
				top: $this.offset().top - $parent.offset().top
			}

			var visible_left_col = Math.ceil(-pos.left / _to.width) - 1,
				visible_top_row = Math.ceil(-pos.top / _to.height) - 1;

			for (var i = visible_left_col; i <= visible_left_col + viewport_cols; i++) {
				for (var j = visible_top_row; j <= visible_top_row + viewport_rows; j++) {					
					if (grid[i] === undefined) {
						grid[i] = {};
					} else if (grid[i][j] === undefined) {
						create_tile(i, j);
					}
				}
			}
		};
		self.update_tiles = update_tiles;

		self.redraw_tiles = function(current_zoom) { // deprecated
			_to.width = _to.width * current_zoom;
			_to.height = _to.height * current_zoom;
			_to.max_height = _to.max_height * current_zoom;
			_to.max_width = _to.max_width * current_zoom;

			if (_do.axis == "x") {
				_to.range_row = [_to.start_row, _to.start_row];
			} else if (_do.axis == "y") {
				_to.range_col = [_to.start_col, _to.start_col];
			}
		
			if (_to.max_height!=null) {
				_to.range_row = [0, Math.floor(_to.max_height/_to.height)];
			}

			if (_to.max_width!=null) {
				_to.range_col = [0, Math.floor(_to.max_width/_to.width)];			
			}

			$draggable.empty();

			var viewport_width = $viewport.width(),
			viewport_height = $viewport.height(),
			viewport_cols = Math.ceil(viewport_width / _to.width),
			viewport_rows = Math.ceil(viewport_height / _to.height);

			$draggable.offset({
				left: $viewport.offset().left - (_to.start_col * _to.width),
				top: $viewport.offset().top - (_to.start_row * _to.height)
			});

			grid = {};
/*
			for (var i = _to.start_col, m = _to.start_col + viewport_cols; i < m && (_to.range_col[0] <= i && i <= _to.range_col[1]); i++) {
				grid[i] = {}
				for (var j = _to.start_row, n = _to.start_row + viewport_rows; j < n && (_to.range_row[0] <= j && j <= _to.range_row[1]); j++) {
					create_tile(i, j);
				}
			}
*/
			/*
			for (var i in grid) {
				for (var j in grid[i]) {					
					//var $e = $draggable.find('>div[col='+i+'][row='+j+']'); // todo : remove all div and recreate then via create_tiles !! + nouveau offset
					//$e.empty();
					//_to.oncreate($e, i, j);
					create_tile(i, j);
				}
			}
			*/
			//update_tiles();
			update_containment();
		};
		
		// Public Methods
		//-----------------
		self.update_containment = update_containment;
		
		self.draggable = function() {
			return $draggable;
		};
		
		self.disabled = function(value) {
			if (value === undefined) {
				return $draggable;
			}
			
			$draggable.draggable("option", "disabled", value);
			
			$draggable.css({ cursor: (value) ? "default" : "move" });
		};
		self.remove = function() {
			//$(window).off("resize",update_containment);	
			self.disabled(true);
		}
		self.setCustomDrag = function(f) {
			_do.custom_drag = f;
		};

		self.getCustomDrag = function() {
			return _do.custom_drag ;
		};

		self.center = function(col, row) {
			var x = _to.width * col,
				y = _to.height * row,
				half_width = _to.width / 2,
				half_height = _to.height / 2,
				half_vw_width = $viewport.width() / 2,
				half_vw_height = $viewport.height() / 2,
				offset = $draggable.offset();
				
			var new_offset = { 
				left: -x - (half_width - half_vw_width), 
				top: -y - (half_height - half_vw_height)
			};
			
			if (_do.axis == "x") {
				new_offset.top = offset.top;
			} else if (_do.axis == "y") {
				new_offset.left = offset.left;
			}
			
			$draggable.offset(new_offset);
						
			update_tiles();
		};

		// Setup
		//--------
		
		var viewport_width = $viewport.width(),
			viewport_height = $viewport.height(),
			viewport_cols = Math.ceil(viewport_width / _to.width),
			viewport_rows = Math.ceil(viewport_height / _to.height);

		$draggable.offset({
			left: $viewport.offset().left - (_to.start_col * _to.width),
			top: $viewport.offset().top - (_to.start_row * _to.height)
		});

		var grid = {};
		for (var i = _to.start_col, m = _to.start_col + viewport_cols; i < m && (_to.range_col[0] <= i && i <= _to.range_col[1]); i++) {
			grid[i] = {}
			for (var j = _to.start_row, n = _to.start_row + viewport_rows; j < n && (_to.range_row[0] <= j && j <= _to.range_row[1]); j++) {
				create_tile(i, j);
			}
		}
		//$(window).resize(update_containment);
/*
		// Handle resize of window.
		$(window).resize(function() {
			// HACK:
			// Update the containment when the window is resized
			// because the containment boundaries depend on the offset of the viewport.
			update_containment();
		});
	*/	
		// The drag event handler.
		
		_do.drag = function(e, ui) {
			
			if (_do.custom_drag) {
				_do.custom_drag(e,ui);
			} else {
				update_tiles();
			}			
		};

		_do.start = function(e,ui) {			
			$.dragMomentum.start(this.id, e.clientX, e.clientY, e.timeStamp);			
        };

		_do.stop= function(e,ui) {
			$.dragMomentum.end(this.id, e.clientX, e.clientY, e.timeStamp,self);
        };
		
		
		$draggable.draggable(_do);
		
		update_containment();
	};
})(jQuery);
