
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

    createWindow : function(){
        var desktop = this.app.getDesktop();
       
           var win = desktop.createWindow({
              
                title:'Editeur',
				y:0,
				x:400,
                width:740,
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
								$.get("aiuXML.xml.txt",function (data) {
									editor.setValue(data);
								});
								
							},
							resize: function(){
								var graph_id = this.id+"-editor";
								this.aceEditor.resize(true);
								//resizeSVG(graph_id);
							}
						}
					}
				],
				
				
				
				
				
				listeners: {
					afterrender:function() {
						var me = this;
						
					}
				},
				// count : 'absolute', 'percent', order_by : 'modalite' or 'marge'
				
				
				dockedItems: [
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
										{
											xtype: 'pagingtoolbar',
											config: {
												pageSize: 10,
												totalRecordCount: 234
											},
											store:  Ext.create('Ext.data.Store', {
													id:'simpsonsStore',
													autoLoad: false,
													fields:[''],
													pageSize: 10, // items per page
													listeners:{
														beforeload:function() {
															console.log("load the store");
															return false;
														}
													},
													totalRecords:20,
													count:30,
													total:40
												}),
											listeners: {
												afterrender: function() {
													this.down("#refresh").hide();
													console.log(this);
													
												}
											},
											moveNext: function() {
												console.log("next");
												return true;
											}
							
										},
										{
											xtype: 'cycle',
											text: '',
											prependText: 'Comptage: ',
											showText: true,
											scope: this,
											changeHandler: function(cycle, activeItem){
												console.log("change : ",activeItem.text);	
														
												if (activeItem.text == 'absolue') {
													cycle.up("window").count_in = 'absolute';
												} else {
													cycle.up("window").count_in = 'percent';
												}
											} ,
											menu: {												
												items: [{
													text: 'absolue',
													checked: true													
												}, {
													text: 'pourcentage'													
												}]
											}
										},
										{
											xtype: 'cycle',
											text: '',
											prependText: 'Tri: ',
											showText: true,
											scope: this,
											changeHandler: function(cycle, activeItem){
												if (activeItem.text == 'par modalité') {
													cycle.up("window").order_by = 'modalite';
												} else {
													cycle.up("window").order_by = 'marge';
												}				
											} ,
											menu: {												
												items: [{
													text: 'par modalité',
													checked: true													
												}, {
													text: 'par valeur'													
												}]
											}
										},
										{
											// .x-tbar-page-prev
											// background-image: url("images/grid/page-prev.png")
											xtype: 'button',
											text:' ',
											 cls : 'x-btn-icon-el x-btn-icon-el-plain-toolbar-small x-tbar-page-prev' 
											 
										},
										{
											text:'export',
											listeners: {
												click:function(button) {
													var win = button.up("window");
													location.href="/service/commands?"+$.param( {cmd:'exportDistinctValues',path:[win.current_row_path,win.current_col_path,win.current_count_path],order_by:win.order_by,count_in:win.count_in} );
												}
											}
										}
									]
								}
								
								]
								
               
            });
        
        return win;
    }
});
