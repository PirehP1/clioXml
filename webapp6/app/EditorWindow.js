
Ext.define('Desktop.EditorWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'editor-win',

    init : function(){
        this.launcher = {
            text: 'Editeur',
            iconCls:'icon-grid'
            
        };
    },

    createWindow : function(ligne,colonne,value,confirmedIndividu,current_filtreId){
		
        var desktop = this.app.getDesktop();
       
           var win = desktop.createWindow({
              
                title:'Editeur',
				y:0,
				/*
				x:400,
                width:740,
                height:$(window).height()-48,
				*/
				x:$(window).width()*0.3,
                width:$(window).width()*0.4,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'fit',
						
				items:[
					{
						xtype:'panel',
						
						itemId:'contingence_panel',
						html:'<div style="height:100%;height:100%" name="editor">',
						aceEditor:null,
						listeners:{
							afterrender:function() {
								
								//innerCt																
								//console.log("le svg  :",$("#"+this.id).find("svg"));
								$("#"+this.id).find("div[name='editor']").attr("id",this.id+"-editor");
								
								var langTools = ace.require("ace/ext/language_tools");
								editor = ace.edit(this.id+"-editor");
								this.up("window").editor = editor;
								editor.setOptions({
									enableBasicAutocompletion: true,
									enableSnippets: true,
									enableLiveAutocompletion: false
								});
								editor.setTheme("ace/theme/twilight");
								var Mode = require('ace/mode/xml').Mode;
								var XMLMode = new Mode();
								editor.getSession().setMode(XMLMode);
								
								this.aceEditor = editor;
								initEditor(editor);
								/*
								$.get("aiuXML.xml.txt",function (data) {
									editor.setValue(data);
								});
								*/
								
							},
							resize: function(){
								var graph_id = this.id+"-editor";
								this.aceEditor.resize(true);
								//resizeSVG(graph_id);
							}
						}
					}
				],
				
				currentPage:1,
				totalPage:1,
				nb_result_per_page:1,
				
				resetPagination:function(total) {
					//this.down("#nb_reponse").text = total+" réponses";
					this.totalPage = total/this.nb_result_per_page;
					this.down("#pages").setData("1/"+this.totalPage);
				},
				nextPage:function() {
					if (this.currentPage == this.totalPage) return;
					var me = this;
					me.setLoading(true);
					$.post('/service/commands', {cmd:'getIndividuFromDistinctValues',view_mode:this.currentViewMode,path:[ligne,colonne,value],confirmed:JSON.stringify(confirmedIndividu),start:this.currentPage+1,nbResult:1,filtreId:current_filtreId}, function(data, textStatus) {															
							me.displayDoc(data);
							me.currentPage = me.currentPage+1;
							me.down("#pages").setData(me.currentPage+"/"+me.totalPage);
							me.setLoading(false);
						});
				},
				prevPage:function() {
					if (this.currentPage == 1) return;
					var me = this;
					me.setLoading(true);
					$.post('/service/commands', {cmd:'getIndividuFromDistinctValues',view_mode:this.currentViewMode,path:[ligne,colonne,value],confirmed:JSON.stringify(confirmedIndividu),start:this.currentPage-1,nbResult:1,filtreId:current_filtreId}, function(data, textStatus) {															
							me.displayDoc(data);
							me.currentPage = me.currentPage-1;
							me.down("#pages").setData(me.currentPage+"/"+me.totalPage);
							me.setLoading(false);
						});
				},
				firstPage:function() {
					if (this.currentPage == 1) return;
					var me = this;
					me.setLoading(true);
					$.post('/service/commands', {cmd:'getIndividuFromDistinctValues',view_mode:this.currentViewMode,path:[ligne,colonne,value],confirmed:JSON.stringify(confirmedIndividu),start:1,nbResult:1,filtreId:current_filtreId}, function(data, textStatus) {															
							me.displayDoc(data);
							me.currentPage = 1;
							me.down("#pages").setData(me.currentPage+"/"+me.totalPage);
							me.setLoading(false);
						});
				},
				lastPage:function() {
					if (this.currentPage == this.totalPage) return;
					var me = this;
					me.setLoading(true);
					$.post('/service/commands', {cmd:'getIndividuFromDistinctValues',view_mode:this.currentViewMode,path:[ligne,colonne,value],confirmed:JSON.stringify(confirmedIndividu),start:this.totalPage,nbResult:1,filtreId:current_filtreId}, function(data, textStatus) {															
							me.displayDoc(data);
							me.currentPage = me.totalPage;
							me.down("#pages").setData(me.currentPage+"/"+me.totalPage);
							me.setLoading(false);
						});
				},
				currentViewMode:'xml',
				setCurrentViewMode:function(viewMode) {
					this.currentViewMode = viewMode;
					var me = this;
					me.setLoading(true);
					if (viewMode=='xml') {
						var Mode = require('ace/mode/xml').Mode;
						var XMLMode = new Mode();
						this.editor.getSession().setMode(XMLMode);
					} else {
						/*
						var ModeT = require('ace/mode/html').Mode;
						var HTMLMode = new ModeT();
						this.editor.getSession().setMode(HTMLMode);
						*/
						this.editor.getSession().setMode("ace/mode/html");
					}
					$.post('/service/commands', {cmd:'getIndividuFromDistinctValues',view_mode:this.currentViewMode,path:[ligne,colonne,value],confirmed:JSON.stringify(confirmedIndividu),start:this.currentPage,nbResult:1,filtreId:current_filtreId}, function(data, textStatus) {															
							console.log("setCurrentViewMode=",viewMode,data);
							me.displayDoc(data);							
							me.setLoading(false);
						});
				},
				displayDoc: function(data) {
					var index_s = data.baseUri[0].indexOf("/")+1;
					var baseuri = data.baseUri[0].substring(index_s,data.baseUri[0].length-9);
					var path = data.paths[0].substring(8,data.paths[0].length-9);
					var doc="";	
					if (this.currentViewMode == 'xml') {
						doc = data.result[0].substring(10,data.result[0].length-11); // on enleve le tag "<result>"
						//this.editor.setValue(xmlToString(doc.firstChild));
					} else {
						doc = data.result[0];
					}
					this.editor.setValue(doc);
					
						var n = removeBracket(removeQName(path));
						
						n = n.substring(1).split("/");
						n.pop();
						n.reverse();
						var n2=[];
						for (var t=0;t<n.length;t++) {
							n2.push({tagName:n[t]});
						}
						this.editor.rootPath = n2;
						
					this.down('#node_path').setValue(baseuri+path);
					this.editor.scrollToLine(1, true, true, function () {});
					this.editor.gotoLine(1, 1, true);
				},
				listeners: {
					afterrender:function() {
						
						var me = this;
						var start= 1;
						me.setLoading(true);
						$.post('/service/commands', {cmd:'getIndividuFromDistinctValues',view_mode:this.currentViewMode,path:[ligne,colonne,value],confirmed:JSON.stringify(confirmedIndividu),start:start,nbResult:1,filtreId:current_filtreId}, function(data, textStatus) {	
							me.resetPagination(data.total);
							
							me.displayDoc(data);
							me.setLoading(false);
						});
					}
				},
				// count : 'absolute', 'percent', order_by : 'modalite' or 'marge'
				
				
				dockedItems: [
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
									/*
									{
										xtype: 'tbtext', text: '43 réponses',itemId:'nb_reponse'
									},
									*/
									{
										icon: "theme/neptune/images/grid/page-first.png",										
										xtype: 'button',
										listeners: {
												click:function(button) {
													this.up("window").firstPage();
												}
											}
									},
									{
										icon: "theme/neptune/images/grid/page-prev.png",										
										xtype: 'button',
										listeners: {
												click:function(button) {
													this.up("window").prevPage();
												}
											}
									},
									{
										xtype:'textfield',
										//fieldLabel:'document',
										value:'1/10',
										labelWidth:45,
										grow:true,
										itemId:'pages'
									},
									{
										icon: "theme/neptune/images/grid/page-next.png",										
										xtype: 'button',
										listeners: {
												click:function(button) {
													this.up("window").nextPage();
												}
											}
									},
									{
										icon: "theme/neptune/images/grid/page-last.png",										
										xtype: 'button',
										listeners: {
												click:function(button) {
													this.up("window").lastPage();
												}
											}
									},
										
										{
										text: 'Actions',                      
										menu: {
											xtype: 'menu',                          
											items: [{
													text: 'Vue',
													menu : [ 
															{
																xtype: 'radiogroup',
																columns: 1,
																vertical: true,
																items: [
																	{ 
																		boxLabel: 'XML', 
																		name: 'vue', 
																		inputValue: 'xml' ,
																		checked:true
																	},
																	{ 
																		boxLabel: 'Text', 
																		name: 'vue', 
																		inputValue: 'text' 
																	}           
																],
																listeners: {
																	change: function (field, newValue, oldValue) {
																			console.log("onchange",field,newValue,oldValue);
																			var win = field.up("window"); 
																			win.setCurrentViewMode(newValue.vue);
																			field.up("menu").up("menu").hide();																		
																	}
																}
															}
														]
												}, {
													text: 'Export XML',
													scale: 'small',
													listeners: {
														click: function(item){											    
															
															var f = $("#formDownload");				
															f.empty();
															
															f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("downloadIndividuFromDistinctValues"));			
															f.append($("<input>").attr("type", "hidden").attr("name", "path[]").val(ligne));
															f.append($("<input>").attr("type", "hidden").attr("name", "path[]").val(colonne));
															f.append($("<input>").attr("type", "hidden").attr("name", "path[]").val(value));
															f.append($("<input>").attr("type", "hidden").attr("name", "format").val("xml"));
															f.append($("<input>").attr("type", "hidden").attr("name", "confirmed").val(JSON.stringify(confirmedIndividu)));
															f.append($("<input>").attr("type", "hidden").attr("name", "filtreId").val(current_filtreId));
															f.submit();
														}
													}
												}]                          
										}
									}
									]
								},
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
										{ xtype:'textfield',flex:1,fieldLabel:'chemin racine',itemId:"node_path",editable:false}
									]
								}
								]
								
               
            });
        
        return win;
    }
});
