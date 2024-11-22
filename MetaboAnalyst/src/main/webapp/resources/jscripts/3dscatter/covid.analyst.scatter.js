/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var currentOmicsType = "";
var outlineBool = false;
var scene2;
var sphereOpacity = 0.8;
var sphereColor = "#d6d6d6";
var sphereMesh1;
var sphereMesh2;
var current_module = "NA";
var row_selection_on_user_click = true;
var current_editing_row = "";
var comp_last_selected_table = "";//rows from which table to consider for comparison test
var labelArr = {};
var twoDMode = false;
var bundled = false;
var animationBool = false;
var compRes = [];
var lineObj = {};
var labelNodeMode = false;
var mainCheckBool = true;
var initScene2 = true;
var meta_arr = [];
var contours_info_obj = {};
var cluster_arr = [];
var current_halo_color = "#222222";
var current_encasing_color = "#222222";
var takeImageBool = false;
var embeddedCollapsed = true;//if condition is in 3d-forcegraph animation loop ctrl-f: setviewport
var current_main_plot = "score";// score or loading in the main plot
var nav_arr = [];// array of meshes in navigation plot
var nav_nodes_arr = []; // array of nodes in nav plot
var shape_type = "contour";
var scatterType = "single";
var curShape = "sphere";
var planeU = {};
var cubeU = {};
var layerCol = "#666666";
var wallCol = "#666666";
var axisColorU = "#222222";
var arrowColorU = "#ff0000";
var axesArr = {};
var currentOrigin = v(0, 0, 0);
var Scatter = "abc";
var sub_modules = "";
var selectedMeta = [];
var myTexts = {};
var explodingBool = false;
var clusterMeshes = {};
var boundingClustersGroup = {};
var explodedBool = false;
var labelMode = "global";
var nodes_num = 0;
var fixedLabels = false;
var htmlTextBool = false;
var current_point_color = '#e6194b';
var metaSphereBool = false;
var imagePath = '../../../resources/images/';
var distance2D = 50;
var boundingClustersMesh = {};
var takeVideo = false;
var renderingFinished = true;
var spriteysCanvas = {};
var screenshotBool = false;
var animationVal = 601;
var radius = 1000, theta = 0;
var activeModules = {};
var initPos;
var labelContainerElem;
var bheight;
var curr_mode = "metasphere";
var mouseY;
var mouseX;
var elem;
var idsArr = [];
var camera;
var edgeWidth = 0;
var prevNode = "";
var colHover = "";
var highlightType = "halo";
var outlineUIs = {};
var labeling_threshold;
var curved = false;
var hideLabel = false;
var labelColor = "black";
var labelColor2 = "black";
var camDefaultPos;
var bgColor = "#ffffff";
var spriteys = {};
var usr_dir;
var node_rows = [];
var node_rows_csize = [];
var highlightColor = '#0080ff';
var highlightSize = 30;
var currentEnrichFile = "";
var greyColor = '0x808080';
var focus_enr_anot;
var focus_fun_anot;
var current_fun_nodes;
var dim_mode = false;
var scope_mode = "single";
var nodes_number;
var planeLayer = false;
var planeArr = [];
var planeArr2 = [];
var current_color_attr = "degree";
var curr_omics = "scatter3d_0.json";
var nodeUIs = [];
var camDefaultPosClone;
var scopeh_mode = "single";
var edgeColor = "#d3d3d3";
var spherical = false;
var edgeOpacity = 0.8;
var nodeOpacity = 1;
var labelColSpectrum = new Rainbow();
var viewMode = "default";
var bgColor2 = "#042e60";
var isReductOptSingle = false;
var textNodeObj = {};

//Above are old variables from omics analyst, TODO: clean in-deep later; Below are new ones, always kept!
// All of them are clearly annotated as below
// 1. metaMembers is used to store the infor meta members of one meta data
var metaMembers = [];
// 2. metaSets is used to store the options for SIX meta data ["Platform", "Blood", "Polarity", "Country", "Population", "Comparison"]
var metaSets = {Platform:["All"], Blood:["All"], Polarity:["All"], Country:["All"], Population:["All"], Comparison:["All"]}; 
// 3. finalSets is used to store the datasets user customized for further analysis
var finalSets = [];

function gohome(){
    res = $.ajax({
        type: "GET",
        url: '/MetaboAnalyst/faces/AjaxCall?function=dologout',
        async: false
    }).responseText;
    if(res === "done"){
        window.parent.location.href = "/MetaboAnalyst/home.xhtml";   
    }
}

function initScatter3D() {
    usr_dir = $.ajax({
        type: "GET",
        url: '/MetaboAnalyst/faces/AjaxCall?function=getusrdir' + "&ignoreMe=" + new Date().getTime(),
        async: false
    }).responseText;

    curr_omics = "tsne.json";

    analType = "";
    naviString = "Visual Analytics";

    labelColSpectrum.setSpectrum("#ffffff", "#222222");
    labelColSpectrum.setNumberRange(0, 100);
    console.log(usr_dir + "/" + curr_omics);

    setupNetworkFunctions();
    labelContainerElem = document.querySelector('#labels');
   
    $.getJSON(usr_dir + "/" + curr_omics, function (raw_data) {

        // $.getJSON(usr_dir + "/" + curr_omics, function (raw_data) {
        currFileNm = curr_omics;
        gData = raw_data;
        console.log("here is gData");
        console.log(gData);
        $("#initdialog").dialog('open');
        if (gData.loading !== "NA" && gData.loading !== undefined) {
            gData.navigation = gData.loading;

            var arr;
            isReductOptSingle = ["diablo", "spls"].indexOf(gData.reductionOpt) !== -1;
            if (isReductOptSingle) {
                arr = [gData.nodes, gData.navigation];

                for (var i = 0; i < gData.omicstype.length; i++) {
                    if (gData[gData.omicstype[i]] !== undefined) {
                        arr.push(gData[gData.omicstype[i]]);
                    }
                    arr.push(gData["pca_" + gData.omicstype[i]]);
                    arr.push(gData["pca_" + gData.omicstype[i] + "_loading"]);
                }

                gData[gData.omicstype[0]] = gData.nodes;
                document.getElementById("omicstype").disabled = 'true';
            } else {
                arr = [gData.nodes, gData.navigation];
                for (var i = 0; i < gData.omicstype.length; i++) {
                    arr.push(gData["pca_" + gData.omicstype[i]]);
                    arr.push(gData["pca_" + gData.omicstype[i] + "_loading"]);
                }
            }

            for (var j = 0; j < arr.length; j++) {
                for (var i = 0; i < arr[j].length; i++) {
                    var n = arr[j][i];
                    n.origPosX = n.fx;
                    n.origPosY = n.fy;
                    n.origPosZ = n.fz;

                    n.tcolor = n.colorb;
                    n.color = n.colorb;
                    if (arr[j] !== gData.navigation) {
                        n.tsize = (1 / Math.cbrt(arr[j].length)) * 120;
                        n.size = (1 / Math.cbrt(arr[j].length)) * 120;
                    } else {
                        if (gData.nodes.length > 500) {
                            n.tsize = n.size / 2;
                        } else {
                            n.tsize = n.size;
                        }
                    }
                    n.expcolor = n.expcolb;
                    n.opacity = 1;

                    delete n.x;
                    delete n.y;
                    delete n.z;
                    delete n.vx;
                    delete n.vy;
                    delete n.vz;
                }
            }
            gData["both"] = gData.nodes;
        } else {
            embeddedCollapsed = true;
            gData.navigation = "NA";
        }
        for (var i = 0; i < gData.nodes.length; i++) {
            var n = gData.nodes[i];
            n.origPosX = n.fx;
            n.origPosY = n.fy;
            n.origPosZ = n.fz;
            n.tcolor = n.colorb;
            n.color = n.colorb;
            n.tsize = (1 / Math.cbrt(gData.nodes.length)) * 150;
            n.size = (1 / Math.cbrt(gData.nodes.length)) * 150;
            n.expcolor = n.expcolb;
            n.opacity = 1;

            delete n.x;
            delete n.y;
            delete n.z;
            delete n.vx;
            delete n.vy;
            delete n.vz;
        }

        if (gData.reductionOpt === "mcia") {
            for (var i = 0; i < gData.nodes.length; i++) {
                var n = gData.nodes[i]
                if (n.color === "#D3D3D3") {
                    n.size = 1;
                }
            }
        }

        gData.links = [];

        initData(gData);
        initNet();
        if (gData.navigation !== "NA") {
            initScatter2();
            addLoadingSphere(scene2, 500, sphereColor, sphereOpacity);

            if (isReductOptSingle) {
                scene2.traverse(function (child) {
                    if (child instanceof THREE.Mesh) {
                        var n = child.userData.nodeData;
                        if (n !== undefined) {
                            if (!n.omicstype.includes(gData.omicstype[0])) {
                                child.visible = false;
                            } else {
                                child.visible = true;

                            }
                        }
                    }
                });
            }
        }

        Scatter.renderer().domElement.addEventListener('mousedown', function () {
            animationVal = 601;
        }, false);
        setupClusteringOpts();
    });
}

function setupNetworkFunctions() {
    setTimeout(function () {
        var sel3 = document.getElementById("loadingOpt");
        removeOptions(sel3);
        var option = document.createElement("option");
        var option = document.createElement("option");
        option.text = "Compound Table";
        option.value = "cmpddt";
        sel3.add(option);
        sel3.delete;
    }, 2000);

    $('#nodeLabelBn').bind('click keypress', function (event) {
        var val = $("#labelOpt").val();

        if (val === "show") {
            fixedLabels = false;
            hideLabel = false;

            for (var propertyName in myTexts) {
                //spriteys[propertyName].style.display = "block";
                myTexts[propertyName].material.visible = true;
            }


        } else {
            fixedLabels = false;
            hideLabel = true;
            for (var propertyName in myTexts) {
                //spriteys[propertyName].style.display = "none";
                if (myTexts[propertyName] !== undefined) {
                    myTexts[propertyName].material.visible = false;
                }
            }
        }
        $('#optdlg').dialog('close');
    });

    $("#axisOpt").val("bottom");

    $('#batchBn').bind('click keypress', function (event) {
        highlightMyNodes();
    });

    window.addEventListener('resize', onWindowResize, false);

    $('#nodeSizeBn').bind('click keypress', function (event) {

        var type = $('#nodeOpt').val();
        var val = $('#sizeOpt').val();
        if (val === "increase") {
            if (type === "all") {
                Scatter.graphData().nodes.forEach(function (n) {
                    if (viewMode.includes("text")) {
                        setTextNodeSize(n.__threeObj, true)
                    } else {
                        sizeNodeObj(n, n.__threeObj.scale.x * 1.1)
                    }

                });
            } else {
                Scatter.graphData().nodes.forEach(function (n) {
                    if (n.highlight) {
                        if (viewMode.includes("text")) {
                            setTextNodeSize(n.__threeObj, true)
                        } else {
                            sizeNodeObj(n, n.__threeObj.scale.x * 1.1)
                        }
                    }
                });
            }

        } else {
            if (type === "all") {
                Scatter.graphData().nodes.forEach(function (n) {

                    if (viewMode.includes("text")) {
                        setTextNodeSize(n.__threeObj, false)
                    } else {
                        sizeNodeObj(n, n.__threeObj.scale.x * 0.9)
                    }


                });
            } else {
                Scatter.graphData().nodes.forEach(function (n) {
                    if (n.highlight) {
                        if (viewMode.includes("text")) {
                            setTextNodeSize(n.__threeObj, false)
                        } else {
                            sizeNodeObj(n, n.__threeObj.scale.x * 0.9)
                        }
                    }

                });
            }
        }
    });

    $('#nodeSizeBn2').bind('click keypress', function (event) {
        var curType = current_module.split(/_(.+)/)[0]
        var curSelection = current_module.split(/_(.+)/)[1]
        if (curType === "cluster") {
            var row = $('#mdg').datagrid('getRows')[curSelection];
        } else {
            var row = $('#metadg').datagrid('getRows')[curSelection];
        }
        var ids_arr = row.ids
        var val = $('#sizeOpt2').val();

        if (val === "increase") {
            Scatter.graphData().nodes.forEach(function (n) {
                if (ids_arr.indexOf(n.id) !== -1) {
                    sizeNodeObj(n, n.__threeObj.scale.x * 1.1)
                }
            });
        } else {
            Scatter.graphData().nodes.forEach(function (n) {
                if (ids_arr.indexOf(n.id) !== -1) {
                    sizeNodeObj(n, n.__threeObj.scale.x * 0.9)
                }
            });
        }
    });

    $('#moreOpts').bind('click keypress', function (event) {
        //open advanced dialog
        $('#optdlg').dialog('open');
    });

    $('#metaopt').change(function () {
        curr_meta = $('#metaopt').val();
        
        var dg = $('#metadg');
        var opts = dg.datagrid('options');
        var pager = dg.datagrid('getPager');
        
        opts.pageNumber = 1;
        opts.pageSize = 10;
        pager.pagination('refresh', {
            pageNumber: 1,
            pageSize: 10
        });
        loadMeta();
    });

    $('#exportOpt').change(function () {
        var val = $(this).val();
        if (val === "png") {
            setTimeout(function () {
                takeScreenshot(false);

            }, 100);
        } else if (val === "gif") {
            $('#animationdlg').dialog('open');
        }
    });

    $('#presetColOpt').change(function () {
        var val = $(this).val();
        if (val === viewMode) {
            return;
        }
        $('#loader').show();
        if (val === "text") {
            if (viewMode === "textMeta") {
                Scatter.graphData().nodes.forEach(function (nd) {
                    //opaNodeObjForLabelToggle(nd, 1)
                    removeTextNodeObj(nd.__threeObj)
                    //colorNodeObj(n, n.tcolor);
                });
                hideLabel = false;
            }
            hideLabel = true;
            Scatter.graphData().nodes.forEach(function (nd) {
                if (nd.metatype !== "mcia.seg" && nd.metatype !== undefined) {
                    opaNodeObjForLabelToggle(nd, 0)
                    addTextNodeObj(nd, 48, "name") // scene1 or scene2
                }
            });

        } else if (val === "textMeta") {
            if (viewMode === "text") {
                Scatter.graphData().nodes.forEach(function (nd) {
                    //opaNodeObjForLabelToggle(nd, 1)
                    removeTextNodeObj(nd.__threeObj)
                    //colorNodeObj(n, n.tcolor);
                });
                hideLabel = false;
            }
            hideLabel = true;
            Scatter.graphData().nodes.forEach(function (nd) {
                if (nd.metatype !== "mcia.seg" && nd.metatype !== undefined) {
                    opaNodeObjForLabelToggle(nd, 0)
                    addTextNodeObj(nd, 48, "meta") // scene1 or scene2
                }
            });

        } else {
            //need to go back to node view
            if (viewMode === "text" || viewMode === "textMeta") {
                Scatter.graphData().nodes.forEach(function (nd) {
                    opaNodeObjForLabelToggle(nd, 1)
                    removeTextNodeObj(nd.__threeObj)
                    //colorNodeObj(n, n.tcolor);
                });
                hideLabel = false;
            }

            //var nms = Object.keys(boundingClusters)
            //for (var i = 0; i < nms.length; i++) {
            //    var nm = nms[i]
            //    deleteMetaSphere(nm, Scatter.scene())
            //}

            if (val === "plain") {
                Scatter.graphData().nodes.forEach(function (n) {
                    colorNodeObj(n, "#d6d6d6");
                });
            } else { //default
                Scatter.graphData().nodes.forEach(function (nd) {
                    colorNodeObj(nd, nd.tcolor);
                });
            }
        }
        viewMode = val;
        $('#loader').hide();
    });
   
    $('#styleOpt').change(function () {
        var val = $(this).val();
        var url = usr_dir + "/" + curr_omics;
        if (val === "na") {
            return;
        } else if (val === "label") {
            $('#nodelabeldlg').dialog('open');
        } else if (val === "color") {
            $('#nodecoldlg').dialog('open');
        } else if (val === "size") {
            $('#nodesizedlg').dialog('open');
        } else if (val === "shade") {
            $('#shadingdlg').dialog('open');
        } else if (val === "opacity") {
            if (viewMode.includes("text")) {
                $.messager.alert('Error', 'Not available for text-based node display. Please switch to another mode before using this feautre.', 'error');
                return;
            }
            $('#nodeopacitydlg').dialog('open');
        } else if (val === "topo") {
            if (curr_mode === "single") {
                changeNet(url)
            }
            $('#nodecoldlg').dialog('open');
        } else {
            $('#loader').show();
            if (val === "text") {
                if (viewMode === "textMeta") {
                    Scatter.graphData().nodes.forEach(function (nd) {
                        //opaNodeObjForLabelToggle(nd, 1)
                        removeTextNodeObj(nd.__threeObj)
                        //colorNodeObj(n, n.tcolor);
                    });
                    hideLabel = false;
                }
                hideLabel = true;
                Scatter.graphData().nodes.forEach(function (nd) {
                    if (nd.metatype !== "mcia.seg" && nd.metatype !== undefined) {
                        opaNodeObjForLabelToggle(nd, 0)
                        addTextNodeObj(nd, 48, "name") // scene1 or scene2
                    }
                });

            } else if (val === "textMeta") {
                if (viewMode === "text") {
                    Scatter.graphData().nodes.forEach(function (nd) {
                        //opaNodeObjForLabelToggle(nd, 1)
                        removeTextNodeObj(nd.__threeObj)
                        //colorNodeObj(n, n.tcolor);
                    });
                    hideLabel = false;
                }
                hideLabel = true;
                Scatter.graphData().nodes.forEach(function (nd) {
                    if (nd.metatype !== "mcia.seg" && nd.metatype !== undefined) {
                        opaNodeObjForLabelToggle(nd, 0)
                        addTextNodeObj(nd, 48, "meta") // scene1 or scene2
                    }
                });

            } else {
                //need to go back to node view
                if (viewMode === "text" || viewMode === "textMeta") {
                    Scatter.graphData().nodes.forEach(function (nd) {
                        opaNodeObjForLabelToggle(nd, 1)
                        removeTextNodeObj(nd.__threeObj)
                        //colorNodeObj(n, n.tcolor);
                    });
                    hideLabel = false;
                }

                //var nms = Object.keys(boundingClusters)
                //for (var i = 0; i < nms.length; i++) {
                //    var nm = nms[i]
                //    deleteMetaSphere(nm, Scatter.scene())
                //}

                if (val === "plain") {
                    Scatter.graphData().nodes.forEach(function (n) {
                        colorNodeObj(n, "#d6d6d6");
                    });
                } else { //default
                    Scatter.graphData().nodes.forEach(function (nd) {
                        colorNodeObj(nd, nd.tcolor);
                    });
                }
            }
            viewMode = val;
            $('#loader').hide();
        }
    });

    $('#viewFocusOpt').change(function () {
        var val = $(this).val();

        embeddedCollapsed = true;
        if (val === "biplot") {
            $('#biplotdlg1').dialog('open');
        } else if (val === "biplot-load") {
            $('#biplotdlg2').dialog('open');
        } else {

            $('#loader').show();
            if (val !== current_main_plot) {
                if (val === "score") {
                    switchToScore();
                } else {
                    switchToLoading();
                }
            }

            var nms = Object.keys(boundingClusters);
            for (var i = 0; i < nms.length; i++) {
                var nm = nms[i];
                deleteMetaSphere(nm, Scatter.scene());
            }

            var nms = Object.keys(biplotArrows);
            for (var i = 0; i < nms.length; i++) {
                var nm = nms[i];
                deleteMesh(biplotArrows[nm], Scatter.scene());
                delete biplotArrows[nm];
            }
            nms = Object.keys(biplotLabels);
            for (var i = 0; i < nms.length; i++) {
                var nm = nms[i];
                deleteMesh(biplotLabels[nm], Scatter.scene());
                delete biplotLabels[nm];
            }
            setTimeout(function () {
                $('#loader').hide();
            }, 10);
        }

        outlineBool = false;

    });

    $('#viewSelOpt').change(function () {
        var val = $(this).val();
        if (val === viewMode) {
            return;
        }

        if (val === "auto") {
            if (!isReductOptSingle) {
                document.getElementById('biplotRank1').style.display = "table-row";
                document.getElementById('biplotRank2').style.display = "table-row";
            }
            document.getElementById('biplotBatch').style.display = "none";
        } else if (val === "custom") {
            document.getElementById('biplotRank1').style.display = "none";
            document.getElementById('biplotRank2').style.display = "none";
            document.getElementById('biplotBatch').style.display = "table-row";
        }
        viewMode = val;
    });

    $('#resetBn').bind('click keypress', function (event) {
        //removeAllOverlays();
        resetNetwork();        
    });

    $('#nodeStyleOpt').change(function () {
        var val = $(this).val();
        if (val === "label") {
            $('#nodelabeldlg').dialog('open');
        } else if (val === "color") {
            $('#nodecoldlg').dialog('open');
        } else if (val === "size") {
            $('#nodesizedlg').dialog('open');
        } else if (val === "shade") {
            $('#shadingdlg').dialog('open');
        } else if (val === "opacity") {
            $('#nodeopacitydlg').dialog('open');
        } else if (val === "metanode") {
            collapseMetaNodes();
        } else {
            return;
        }
    });

    $('#switchBn').bind('click keypress', function (event) {
        if (gData.navigation === "NA") {
            $.messager.alert('', "Only score plot is available for this dimension reduction algorithm", 'error');
            return;
        }
        if (current_main_plot === "score") {
            switchToLoading();
        } else {
            switchToScore();
        }
    });

    $('#collapseBn').bind('click keypress', function (event) {
        if (gData.navigation === "NA") {
            $.messager.alert('', "Only score plot is available for this dimension reduction algorithm", 'error');
            return;
        }
        if (embeddedCollapsed) {
            embeddedCollapsed = false;
        } else {
            embeddedCollapsed = true;
        }

    });

    $('#rotateMoveBn').bind('click keypress', function (event) {
        var controls = Scatter.controls()
        controls.mouseButtons = {ORBIT: THREE.MOUSE.LEFT, ZOOM: THREE.MOUSE.MIDDLE, PAN: THREE.MOUSE.RIGHT};
    });

    $('#planeMoveBn').bind('click keypress', function (event) {
        var controls = Scatter.controls()
        controls.mouseButtons = {ORBIT: THREE.MOUSE.RIGHT, ZOOM: THREE.MOUSE.MIDDLE, PAN: THREE.MOUSE.LEFT};
    });

    $('#selectBn').bind('click keypress', function (event) {
        initFilter();
        $('#filterdlg').dialog('open');
    });

    var timeout;
    $('#zoomInBn').bind('mousedown', function (event) {
        timeout = setInterval(function () {
            zoomIn(50);
            zoomCheck();
            updateLabel();
        }, 5);

        return false;
    });

    $('#zoomInBn').bind('mouseup', function (event) {
        clearInterval(timeout);
        return false;
    });

    $('#zoomOutBn').bind('mouseup', function (event) {
        clearInterval(timeout);
        return false;
    });

    $('#zoomOutBn').bind('mousedown', function (event) {
        timeout = setInterval(function () {
            zoomOut(50);
            zoomCheck();
            updateLabel();
        }, 5);
    });

    $("#custom").spectrum({
        color: highlightColor,
        showInitial: true,
        change: function (color) {
            highlightColor = color.toHexString();
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['black', 'white'],
            ['#666666', '#0080ff']
        ]
    });
    
    $("#custommeta").spectrum({
        color: highlightColor,
        showInitial: true,
        change: function (color) {
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['black', 'white'],
            ['#666666', '#0080ff']
        ]
    });

    $("#customAxis").spectrum({
        color: "#222222",
        showInitial: true,
        change: function (color) {
            axisColorU = color.toHexString();
            var sc;
            if (mainCheckBool) {
                sc = Scatter.scene();
            } else {
                sc = scene2;
            }
            generateAxes(sc);
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['black', 'white'],
            ['#666666', '#0080ff']
        ]
    });
    
    $("#customShadow").spectrum({
        disabled: true,
        color: "#D3D3D3"
    });

    $("#customSphere").spectrum({
        color: sphereColor,
        showInitial: true,
        change: function (color) {
            var col = color.toHexString();
            sphereColor = col;
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['black', 'white'],
            ['#666666', '#0080ff']
        ]
    });

    $("#customLayer").spectrum({
        color: "#666666",
        showInitial: true,
        change: function (color) {
            layerCol = color.toHexString();

            var sc;
            if (mainCheckBool) {
                sc = Scatter.scene();
            } else {
                sc = scene2
            }

            if ($('#flCheckbox')[0].checked) {
                initPlane(sc);
            }

        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['black', 'white'],
            ['#666666', '#0080ff']
        ]
    });

    $('#axisCheckbox').change(function () {
        var sc;
        if (mainCheckBool) {
            sc = Scatter.scene();
        } else {
            sc = scene2;
        }

        if (this.checked) {
            generateAxes(sc);
        } else {
            removeAxes(sc);
        }

    });

    $('#axisType').change(function () {
        var sc;
        var val = $('#axisType').val();       
        if (mainCheckBool) {
            sc = Scatter.scene();
        } else {
            sc = scene2;
        }

        if (val === "na") {
            removeAxes(sc);
            for (var propNm in myTexts) {
                myTexts[propNm].visible = false;
            }
        } else {
            generateAxes(sc);
            generateAxisLabel(val, sc);
        }

    });

    $('#animationBn').bind('click keypress', function (event) {
        if (twoDMode) {
            $.messager.alert('Error', 'Only available in 3D mode.', 'error');
            return;
        }
        animationVal = 0;
    });

    $('#updateOpacity').bind('click keypress', function (event) {
        var val = parseFloat($('#edgeSliderOpa').val());
        edgeOpacity = val;
        Scatter.graphData().links.forEach(function (l) {
            l.opacity = val;
            opaLineObj(l, val);
        });

        $('#edgeopacitydlg').dialog('close');
    });

    $('#updateNodeOpacity').bind('click keypress', function (event) {
        var val = parseFloat($('#nodeSliderOpa').val());
        nodeOpacity = val;
        Scatter.graphData().nodes.forEach(function (n) {
            opaNodeObj(n, val);
        });

        $('#nodeopacitydlg').dialog('close');
    });

    $('#platformType').bind('click keypress', function (event){
        //this entry is used to dedine a single platform
        var platftype = $('#platformType').val();
        if(platftype === "lcmsu"){
            metaSets.Platform = "LC-MSu";
        } else if(platftype === "lcmst") {
            metaSets.Platform = "LC-MSt";
        } else if(platftype === "gcms") {
            metaSets.Platform = "GC-MS";
        } else if(platftype === "dims") {
            metaSets.Platform = "DI-MS";
        } else if(platftype === "nmr") {
            metaSets.Platform = "NMR";
        } else if(platftype === "cems") {
            metaSets.Platform = "CE-MS";
        } else if(platftype === "mix") {
            metaSets.Platform = "Mix";
        } else if(platftype === "all") {
            metaSets.Platform = ["LC-MSu", "LC-MSt", "GC-MS", "DI-MS", "NMR", "CE-MS", "Mix"];
        }
        highlightCustdsetscolor(metaSets);
    });

    $('#platformm').bind('click keypress', function (event){//m--> multiple
        //this entry is used to open a dialog to dedine multiple platform
        $("#platformdlg").dialog('open');
    });
    
    $('#ctopt').bind('click keypress', function (event){        
        $("#countrydlg").dialog('open');
    });
    
    $('#ctoptbox').change(function () {
        if (this.checked) {
            metaSets.Country = "All";
        }
    });

    $('#sampleType').change(function(){
        var smptype = $('#sampleType').val();
        metaSets.Blood = smptype;
        highlightCustdsetscolor(metaSets);
    });
    
    $('#chemiType').change(function(){
        var chemitype = $('#chemiType').val();
        metaSets.Polarity = chemitype;
        highlightCustdsetscolor(metaSets);
    });
    
    $('#agepopu').combobox({
        onChange: function (newValue, oldValue) {
            updatePopulation(newValue);
        }
    });
        
    $('#compariType').change(function () {
        //use to control covid comparison panel
        var currcomp = $('#compariType').val();        
        if(currcomp === "severities") {
            enableSeverity();
            $('#outcometable').css("display", "none");
            $('#diseasetable').css("display", "none");
            $('#severitytable').css("display", "block");
        } else if (currcomp === "outcomes") {
            enableOutcome();
            $('#diseasetable').css("display", "none");
            $('#severitytable').css("display", "none");
            $('#outcometable').css("display", "block");
        } else if(currcomp === "diseases") {
            enableDisease();
            $('#outcometable').css("display", "none");
            $('#severitytable').css("display", "none");
            $('#diseasetable').css("display", "block");
        } else {
            disableAllCompari();
            metaSets.Comparison = "All";
        }
        highlightCustdsetscolor(metaSets);
    });
    
    $('#dise1').change(function () {
        updateCompari();
    });
    $('#dise2').change(function () {
        updateCompari();
    });
    $('#dise3').change(function () {
        updateCompari();
    });
    $('#dise4').change(function () {
        updateCompari();
    });
    $('#severi1').change(function () {
        updateCompari();
    });
    $('#severi2').change(function () {
        updateCompari();
    });
    $('#severi3').change(function () {
        updateCompari();
    });
    $('#severi4').change(function () {
        updateCompari();
    });
    $('#outc1').change(function () {
        updateCompari();
    });
    $('#outc2').change(function () {
        updateCompari();
    });
    
    $('#generalOpt').change(function () {
        var generalval = $('#generalOpt').val();
        if(generalval === "white"){
            changeBackground("#ffffff", "#ffffff");
        } else if(generalval === "black") {
            changeBackground("#000000", "#000000");
        }
    });

    $("#customNode").spectrum({
        color: "#222222",
        showInitial: true,
        change: function (color) {
            nodeColor = color.toHexString().replace("#", "0x");
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['black', 'white'],
            ['#666666', '#0080ff']
        ]
    });

    $("#customBg").spectrum({
        color: "#222",
        //flat:true,
        showInitial: true,
        cancelText: "", // not shown, as this is not working in flat mode        
        change: function (color) {
            var col = color.toHexString();
            changeBackground(col, col);
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['white', '#905356'],
            ['#38597a', 'black']
        ]
    });

    $("#customBgg").spectrum({
        color: "#222",
        flat: true,
        showInitial: true,
        cancelText: "", // not shown, as this is not working in flat mode        
        change: function (color) {
            var col = color.toHexString();
            var type = $("#bgColType").val();
            if (type === "gradient_dark") {
                changeBackground("#222222", col);
            } else if (type === "gradient_light") {
                changeBackground("#d3d3d3", col);
            } else if (type === "gradient_dark_bottom") {
                changeBackground(col, "#222222");
            } else if (type === "gradient_light_bottom") {
                changeBackground(col, "#d3d3d3");
            } else {
                changeBackground(col, col);
            }

            $('#bgcolordlg').dialog('close');
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['white', '#905356'],
            ['#38597a', 'black']
        ]
    });

    $("#customModule").spectrum({
        color: current_point_color,
        flat: false,
        showInitial: true,
        cancelText: "", // not shown, as this is not working in flat mode        
        change: function (color) {
            var col = color.toHexString()
            current_point_color = col
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['white', '#905356'],
            ['#38597a', 'black']
        ]
    });

    $("#customEncasing").spectrum({
        color: current_encasing_color,
        flat: false,
        showInitial: true,
        cancelText: "", // not shown, as this is not working in flat mode        
        change: function (color) {
            var col = color.toHexString()
            current_encasing_color = col
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['white', '#905356'],
            ['#38597a', 'black']
        ]
    });

    $("#customEdge").spectrum({
        color: bgColor,
        showInitial: true,
        flat: true,
        change: function (color) {

            var col = color.toHexString().replace("#", "0x");
            edgeColor = col;
            var colorValue = parseInt(col.replace("#", "0x"), 16);
            var colored = new THREE.Color(colorValue);

            Scatter.graphData().links.forEach(function (l) {
                colorLineObj(l, edgeColor);
            })

            $('#edgecoldlg').dialog('close');
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['black', 'white'],
            ['#666666', '#0080ff']
        ]
    });

    $("#customLabel").spectrum({
        flat: false,
        color: bgColor,
        showInitial: true,
        cancelText: "", // not shown, as this is not working in flat mode
        change: function (color) {
            var type = $("#axisOpt").val();
            if (mainCheckBool) {
                labelColor = color.toRgbString();
                for (var propertyNm in spriteysCanvas) {
                    spriteysCanvas[propertyNm].color = labelColor;
                }
                for (var propertyNm in labelArr) {
                    labelArr[propertyNm].color = labelColor;
                }
                labelContainerElem.style.color = labelColor;
                generateAxisLabel(type, Scatter.scene())
            } else {
                labelColor2 = color.toRgbString();
                generateAxisLabel(type, scene2)
            }
            $('#nodelabeldlg').dialog('close');
        },
        showInput: true,
        showPalette: true,
        showSelectionPalette: true, // true by default
        palette: [
            ['black', 'white'],
            ['#666666', '#0080ff']
        ]
    });

    $('#metadg').datagrid({
        loadFilter: pagerFilter,
        onLoadSuccess: function (data) {
            $('#metadg').datagrid('getPanel').find('div.datagrid-header input[type=checkbox]').attr('disabled', 'disabled');
        },
        onSelect: function (index, row) {            
            metaMembers.push(row.name);
            colorSelectedMeta(metaMembers, curr_meta);
            displaySelectedMetaSum(row.name, curr_meta);
        },
        onUnselect: function (index, row) {
            emptystatspanel();            
            if(metaMembers.length > 1){ // two or more selected now
                for (var ii = 0; ii < metaMembers.length; ii++) {
                    if (metaMembers[ii] === row.name) {
                        metaMembers.splice(ii, 1);
                    }
                }
                uncolorUnSelectedMeta(row.name, curr_meta);
            } else {
                //only one selected now!
                for (var ii = 0; ii < metaMembers.length; ii++) {
                    if (metaMembers[ii] === row.name) {
                        metaMembers.splice(ii, 1);
                    }
                }
                recolorThisMeta(curr_meta);                
            }
        }
    });
}

function initNet() {

    document.getElementById('loader_init').style.display = 'block';
    if (gData.misc !== "NA") {
        displayReductionInfo();
    }
    var container = document.getElementById("3d-graph");
    var elem = document.getElementById('3d-graph');

    Scatter = ForceGraph3D({controlType: "orbit"})
            (elem)
            .graphData(gData)
            .width(parseFloat(container.clientWidth))
            .linkOpacity(0)
            .linkWidth('width')
            .cooldownTicks(1)//Prevent force layout engine from running
            .nodeColor('color')
            .nodeLabel('label')
            .nodeVal('size')
            .onNodeClick(modifyNode).enableNodeDrag(false);

    var nodes_num = Scatter.graphData().nodes.length;

    Scatter.renderer().gammaInput = true;
    Scatter.renderer().gammaOutput = true;
    Scatter.controls().staticMoving = true;

    const color = 0Xffffff;  // white
    const near = 10;
    labelColOpt = "#222222";
    
    fogBool = false;
    Scatter.backgroundColor("#ffffff");
    var camera = Scatter.camera();

    camDefaultPos = {
        x: camera.position.x,
        y: camera.position.y,
        z: camera.position.z
    };
    camDefaultPosClone = camera.clone();
    Scatter.nodeResolution(16);
    var firstCanvas = document.getElementsByTagName("canvas")[0];
    firstCanvas.width = container.clientWidth;
    firstCanvas.height = container.clientHeight;
    Scatter.renderer().setAnimationLoop(updateLabelPositions);

    //setFDirected();
    initNodeSize();
    setTimeout(function () {
        initRadius = computeSceneRadius() + 500;
        Scatter.cameraPosition(
                {x: initRadius * 1.2, y: initRadius * 1.2, z: initRadius * 1.7}, // new position
                {x: 0, y: 0, z: 0}, // lookAt ({ x, y, z })
                1000  // ms transition duration
                );
    }, 1000);

    setTimeout(function () {
        initCube(Scatter.scene());
        axisColorU = "#222222";
        initCube(scene2);
        axisColorU = "#222222";
        turnOffFloor(true);

        document.getElementById('loader_init').style.display = 'none';

        document.getElementById('loader').style.display = "none";

        setTimeout(function () {
            if (gData.links.length !== 0) {
                Scatter.graphData().links.forEach(function (l) {
                    colorLineObj2(l, l.color);
                    opaLineObj(l, parseFloat(l.opacity));
                });
            }
        }, 1000);
        //$('#p').panel('close')
    }, 1000);
}

//Author: Zhiqiang Pang
function finishmultiplePlatform(){
    var multiPlatforms = [];
    if($("#platformType1").is(':checked')){//LC-MSu
        multiPlatforms.push('LC-MSu');
    }
    if ($("#platformType2").is(':checked')) {//LC-MSt
        multiPlatforms.push('LC-MSt');
    }
    if ($("#platformType3").is(':checked')) {//GC-MS
        multiPlatforms.push('GC-MS');
    }
    if ($("#platformType4").is(':checked')) {//NMR
        multiPlatforms.push('NMR');
    }
    if ($("#platformType5").is(':checked')) {//DI-MS
        multiPlatforms.push('DI-MS');
    }
    if ($("#platformType6").is(':checked')) {//CE-MS
        multiPlatforms.push('CE-MS');
    }
    if ($("#platformType7").is(':checked')) {//Mix
        multiPlatforms.push('Mix');
    }
    
    if(multiPlatforms.length > 1){
        $('#platformType').val("multiple");
    } else if (multiPlatforms.length === 0) {
        $("#platformdlg").dialog("close");
        return;
    } else {
        $('#platformType').val(multiPlatforms[0].replace('-','').toLowerCase());
    }
    metaSets.Platform = multiPlatforms;
    highlightCustdsetscolor(metaSets);
    $("#platformdlg").dialog("close");
    
}

//Author: Zhiqiang Pang
function finishmultiplecountry(){
    var multicountries = [];
    if ($("#ct1").is(':checked')) {//Australia
        multicountries.push("Australia");
    }
    if ($("#ct12").is(':checked')) {//Brazil
        multicountries.push("Brazil");
    }
    if ($("#ct2").is(':checked')) {//China
        multicountries.push("China");
    }
    if ($("#ct22").is(':checked')) {//Colombia
        multicountries.push("Colombia");
    }
    if ($("#ct3").is(':checked')) {//France
        multicountries.push("France");
    }
    if ($("#ct32").is(':checked')) {//Germany
        multicountries.push("Germany");
    }
    if ($("#ct4").is(':checked')) {//India
        multicountries.push("India");
    }
    if ($("#ct42").is(':checked')) {//Italy
        multicountries.push("Italy");
    }
    if ($("#ct5").is(':checked')) {//Kenya
        multicountries.push("Kenya");
    }
    if ($("#ct52").is(':checked')) {//Latvia
        multicountries.push("Latvia");
    }
    if ($("#ct6").is(':checked')) {//Mexico
        multicountries.push("Mexico");
    }
    if ($("#ct62").is(':checked')) {//Sweden
        multicountries.push("Sweden");
    }
    if ($("#ct7").is(':checked')) {//Turkey
        multicountries.push("Turkey");
    }
    if ($("#ct72").is(':checked')) {//Spain
        multicountries.push("Spain");
    }
    if ($("#ct8").is(':checked')) {//UK
        multicountries.push("UK");
    }
    if ($("#ct82").is(':checked')) {//USA
        multicountries.push("USA");
    }
    if(multicountries.length === 0) {
        metaSets.Country = "All";
        $('#ctoptbox').prop('checked', true);
        $("#countrydlg").dialog("close");
        return;
    } else if(multicountries.length !== 16) {
        $('#ctoptbox').prop('checked', false);
    }
    metaSets.Country = multicountries;
    highlightCustdsetscolor(metaSets);
    $("#countrydlg").dialog("close");
}

//Author: Zhiqiang Pang
function verifyCompariGroups(){
    var compari = $('#compariType').val();
    var compariVec = [];
    if(compari === "diseases") {
        if ($('#dise1').is(':checked')) {
            compariVec.push("COVID");
        }
        if ($('#dise2').is(':checked')) {
            compariVec.push("nonCOVID");
        }
        if ($('#dise3').is(':checked')) {
            compariVec.push("HC");
        }
        if ($('#dise4').is(':checked')) {
            compariVec.push("Recovered");
        }
    } else if (compari === "severities") {
        if ($('#severi1').is(':checked')) {
            compariVec.push("Asymptomatic");
        }
        if ($('#severi2').is(':checked')) {
            compariVec.push("Severe");
        }
        if ($('#severi3').is(':checked')) {
            compariVec.push("Mild_Moderate");
        }
        if ($('#severi4').is(':checked')) {
            compariVec.push("Critical");
        }
    } else if (compari === "outcomes") {
        if ($('#outc1').is(':checked')) {
            compariVec.push("Survived");
        }
        if ($('#outc2').is(':checked')) {
            compariVec.push("Deceased");
        }
    } else {
        $.messager.alert('Error', "None of the sample comparison selected!", 'error');
        return false;
    }
    if(compariVec.length > 1){
        metaSets.Comparison = compariVec;
        return true;
    } else {
        metaSets.Comparison = compariVec;
        return false;
    }
}

//Author: Zhiqiang Pang
function confirmDataset() {
    if ($('#compariType').val() === "none") {
        $.messager.alert('Error', "None of the sample comparison selected!", 'error');
        return;
    }
    
    if(!verifyCompariGroups()) {
        $.messager.alert('Error', "At least two groups required for comparison!", 'error');
        return;
    }
    
    finalSets = []; //empty this array
    console.log("pass verification and need furhter confirmation from dialiog!");
    
    $('#datasetdg').datagrid({
        onSelect: function (index, row) {
            finalSets.push(row.name);            
        },

        onUnselect: function (index, row) {            
            for (var ii = 0; ii < finalSets.length; ii++) {
                if (finalSets[ii] === row.name) {
                    finalSets.splice(ii, 1);
                }
            }            
        }
    });
    
    var ConfirmingData = organizeConfirmData();
    var ds_rows = [];    
    for (var i = 0; i < ConfirmingData.DataSets.length; i++) {
        ds_rows.push({
            index: i,
            name: ConfirmingData.DataSets[i],
            size: ConfirmingData.Size[i],
            cmpd: ConfirmingData.CMPD[i]
        });
    }
    $('#datasetdg').datagrid('loadData', ds_rows);    
    $('#confirmdlg').dialog('open');
    return true;
}

//Author: Zhiqiang Pang
function downloadDataset() {    
    if ($('#compariType').val() === "none") {
        $.messager.alert('Error', "None of the sample comparison selected!", 'error');
        return;
    } else { 
        $.messager.progress({
            title: 'Please wait',
            text: 'Processing ...',
            interval: 20
        });
    }
    
    setTimeout(function () {
        var platformVec, BloodTypeVec, PolarityVec, CountryVec, PopulationVec, ComparisonVec = "";
        platformVec = metaSets.Platform.toString();
        BloodTypeVec = metaSets.Blood.toString();
        PolarityVec = metaSets.Polarity.toString();
        CountryVec = metaSets.Country.toString();
        PopulationVec = metaSets.Population.toString();
        ComparisonVec = metaSets.Comparison.toString();

        $.ajax({
            beforeSend: function () {                
            },
            dataType: "html",
            type: "POST",
            url: '/MetaboAnalyst/faces/AjaxCall',
            data: {function: 'generatecoviddata', platforms: platformVec, bloods: BloodTypeVec, polarities: PolarityVec, countries: CountryVec, populations: PopulationVec, comparis: ComparisonVec},
            async: false,
            success: function (result) {
                $.messager.progress('close');
                download(result);
            }
        });
    }, 360);
    
}

//Author: Zhiqiang Pang
function download(url) {
  const a = document.createElement('a');
  a.href = url;
  a.download = url.split('/').pop();
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}

//Author: Zhiqiang Pang
function disableAllCompari() {
    $('#outcometable').css("display", "none");
    $('#diseasetable').css("display", "none");
    $('#severitytable').css("display", "none");
    
    $('#dise1').prop('checked', false);
    $('#dise2').prop('checked', false);
    $('#dise3').prop('checked', false);
    $('#dise4').prop('checked', false);
    $('#severi1').prop('checked', false);
    $('#severi2').prop('checked', false);
    $('#severi3').prop('checked', false);
    $('#severi4').prop('checked', false);
    $('#outc1').prop('checked', false);
    $('#outc2').prop('checked', false);

    $('#dise1').prop('disabled', true);
    $('#dise2').prop('disabled', true);
    $('#dise3').prop('disabled', true);
    $('#dise4').prop('disabled', true);
    $('#severi1').prop('disabled', true);
    $('#severi2').prop('disabled', true);
    $('#severi3').prop('disabled', true);
    $('#severi4').prop('disabled', true);
    $('#outc1').prop('disabled', true);
    $('#outc2').prop('disabled', true); 
}

//Author: Zhiqiang Pang
function enableDisease(){    
    $('#dise1').prop('checked', true);
    $('#dise2').prop('checked', true);
    $('#dise1').prop('disabled', false);
    $('#dise2').prop('disabled', false);
    $('#dise3').prop('disabled', false);
    $('#dise4').prop('disabled', false);
    
    $('#severi1').prop('disabled', true);
    $('#severi2').prop('disabled', true);
    $('#severi3').prop('disabled', true);
    $('#severi4').prop('disabled', true);
    $('#outc1').prop('disabled', true);
    $('#outc2').prop('disabled', true);
    metaSets.Comparison = ['COVID', 'nonCOVID'];
}

//Author: Zhiqiang Pang
function enableSeverity(){    
    $('#severi3').prop('checked', true);
    $('#severi2').prop('checked', true);
    $('#severi1').prop('disabled', false);
    $('#severi2').prop('disabled', false);
    $('#severi3').prop('disabled', false);
    $('#severi4').prop('disabled', false);
    
    $('#dise1').prop('disabled', true);
    $('#dise2').prop('disabled', true);
    $('#dise3').prop('disabled', true);
    $('#dise4').prop('disabled', true);
    $('#outc1').prop('disabled', true);
    $('#outc2').prop('disabled', true);
    metaSets.Comparison = ['Mild_Moderate', 'Severe'];
}

//Author: Zhiqiang Pang
function enableOutcome(){
    $('#outc1').prop('checked', true);
    $('#outc2').prop('checked', true);
    $('#outc1').prop('disabled', false);
    $('#outc2').prop('disabled', false);
    
    $('#dise1').prop('disabled', true);
    $('#dise2').prop('disabled', true);
    $('#dise3').prop('disabled', true);
    $('#dise4').prop('disabled', true);
    $('#severi1').prop('disabled', true);
    $('#severi2').prop('disabled', true);
    $('#severi3').prop('disabled', true);
    $('#severi4').prop('disabled', true);
    metaSets.Comparison = ['Deceased', 'Survived'];
}

//Author: Zhiqiang Pang
function resetallparams() {
    emptystatspanel();
    metaSets = {Platform:["All"], Blood:["All"], 
        Polarity:["All"], Country:["All"], 
        Population:["All"], Comparison:["All"]};
    
    $('#platformType').val("all");
    $('#sampleType').val("All");
    $('#chemiType').val("All");
    $('#ctoptbox').prop('checked', true);
    
    $('#popu1').prop('checked', true);
    $('#popu2').prop('checked', true);
    $('#popu3').prop('checked', true);
    
    $('#compariType').val("none");
    disableAllCompari();
    
    highlightCustdsetscolor(metaSets);
}

//Author: Zhiqiang Pang
function updatePopulation(popus){
    if (popus.length > 0) {
        metaSets.Population = popus;
    } else {
        $.messager.alert('Error', "You have to select at least one population!", 'error');
        $('#agepopu').combobox({
            value: ['Both', 'Adults', 'Children']
        });
        metaSets.Population = "All";
    }
    highlightCustdsetscolor(metaSets);
}

//Author: Zhiqiang Pang
function updateCompari(){
    verifyCompariGroups();    
    if(metaSets.Comparison.length === 0){
        $.messager.alert('Error', "None of the sample groups selected!", 'error');
        if($('#compariType').val() === "diseases"){
            $('#dise1').prop('checked', true);
            metaSets.Comparison = "COVID";
        } else if($('#compariType').val() === "severities") {
            $('#severi2').prop('checked', true);
            metaSets.Comparison = "Severe";
        } else if($('#compariType').val() === "outcomes"){
            $('#outc1').prop('checked', true);
            metaSets.Comparison = "Survived";
        } else if($('#compariType').val() === "none"){
            disableAllCompari();
            metaSets.Comparison = "All";
        }
    }
    highlightCustdsetscolor(metaSets); 
}

//Author: Zhiqiang, Guangyan
function setupClusteringOpts() {
    //clustering option is not an Exact Name here
    //Currently, it is used to gather different meta info of the covid datasets
    var qOpts = $('#metaopt');

    var count = 0;
    var myOpts = '<optgroup label="-- Metadata --">';
    for (var propNm in gData.meta) {
        if (count === 0) {
            curr_meta = propNm;
        }
        if (propNm !== "Populations") {
            myOpts = myOpts + '<option value="' + propNm + '">' + propNm + '</option>';
            count++;
        } else {
            myOpts = myOpts + '<option value="' + propNm + '">Age</option>';
            count++;
        }
    }

    qOpts.append(myOpts);

    curr_meta = $('#metaopt').val();
    loadMeta();
}

//Author: Zhiqiang Pang
function loadMeta() {
    metaMembers = [];
    var mdl_rows = [];
    var arr = gData.meta[curr_meta];

    var setobj = {};
    var setobj_ids = {};
    var kpGroups = ['COVID', 'nonCOVID', 'HC', 'QC', 'Recovered'];
    for (var i = 0; i < arr.length; i++) {
        if(arr[i] === null){arr[i] = "Unknown";}        
        if (setobj[arr[i]] === undefined) {            
            setobj[arr[i]] = 1;
        } else {
            setobj[arr[i]]++;
        }
    }

    var set = [...new Set(arr)];
    
    if (current_main_plot === "score") {
        //Here is to define the id (sample names) for all nodes
        var nodes = Scatter.graphData().nodes;
        for (var i = 0; i < nodes.length; i++) {
            var n = nodes[i];
            n.metatype = arr[i];
            if(curr_meta === "Diseases"){
                if(kpGroups.indexOf(n.metatype) === -1){
                    continue;
                }
            }
            if (setobj_ids[n.metatype] === undefined) {
                setobj_ids[n.metatype] = [n.id];
            } else {
                setobj_ids[n.metatype].push(n.id);
            }
        }
    } else {
        //Maybe used for some other plot types of compound list with this "ELSE"
    }

    if (set.indexOf("Other") !== -1) {
        if (current_main_plot === "score") {
            var nodes = Scatter.graphData().nodes;
            for (var i = 0; i < nodes.length; i++) {
                var n = nodes[i];
                if (setobj_ids["Other"].indexOf(n.id) !== -1) {
                    n.metatype = "Other";
                    if (curr_meta === "omics") {
                        n.type = "Other";
                    }
                }
            }
        } else {
            scene2.traverse(function (child) {
                if (child instanceof THREE.Mesh) {
                    if (child.userData.nodeData !== undefined) {
                        var n = child.userData.nodeData;
                        if (setobj_ids["Other"].indexOf(n.id) !== -1) {
                            child.userData.nodeData.metatype = "Other";
                            if (curr_meta === "omics") {
                                child.userData.nodeData.type = "Other";
                            }
                        }

                    }
                }
            });
        }
    }
    
    var colArr = [];

    $.ajax({
        beforeSend: function () {
            $.messager.progress({
                text: 'Processing .....'
            });
        },
        dataType: "html",
        type: "POST",
        url: '/MetaboAnalyst/faces/AjaxCall',
        data: {function: 'generateColorArray', number: set.length},
        async: false,
        success: function (result) {
            $.messager.progress('close');
            $.getJSON(usr_dir + "/" + result, function (jsonDat) {
                colArr = jsonDat;
                
                var nodes = Scatter.graphData().nodes;
                setTimeout(function (n) {
                    for (var i = 0; i < nodes.length; i++) {
                        var n = nodes[i];
                        var inx = set.indexOf(n.metatype);
                        if (inx !== -1) {
                            n.colorb = colArr[inx];
                            n.tcolor = colArr[inx];
                            colorNodeObj(n, colArr[inx]);
                        }
                    }
                }, 1000);

                for (var i = 0; i < set.length; i++) {
                    if (curr_meta === "Diseases") {
                        if (kpGroups.indexOf(set[i]) === -1) {
                            continue;
                        }
                    }

                    mdl_rows.push({
                        index: i,
                        name: set[i],
                        size: setobj_ids[set[i]].length,
                        color: '<span id=\"meta_' + i + '" style="background-color:' + colArr[i] + '" onclick="openModuleColorPicker(this);event.stopPropagation()">&emsp;&emsp;&emsp;</span>'
                    });
                }
                meta_arr = mdl_rows;                
                $('#metadg').datagrid('loadData', mdl_rows);
            });
        }
    });

    if (curr_meta === "omics") {
        var nodes = Scatter.graphData().nodes;
        for (var j = 0; j < set.length; j++) {
            for (var i = 0; i < nodes.length; i++) {
                var n = nodes[i];
                if (n.metatype === set[j]) {
                    colArr.push(n.color);
                }
            }
        }
        colArr = [...new Set(colArr)];
    } else {
        for (var i = 0; i < set.length; i++) {
            colArr.push("#");
        }
    }
}

//Author: Zhiqiang Pang
function colorSelectedMeta(names, metaNM){    
    var nodes = Scatter.graphData().nodes;
    var metainfo = Scatter.graphData().meta[metaNM];
    for (var i = 0; i < nodes.length; i++) {
        var n = nodes[i];
        if (!names.includes(metainfo[i])) {
            colorNodeObj(n, "#bababa");
        } else {
            colorNodeObj(n, n.colorb);
        }
    }
}

//Author: Zhiqiang Pang
function recolorThisMeta(metaNM) {    
    var nodes = Scatter.graphData().nodes;    
    for (var i = 0; i < nodes.length; i++) {
        var n = nodes[i];
        colorNodeObj(n, n.colorb);
    }
}

//Author: Zhiqiang Pang
function uncolorUnSelectedMeta(name, metaNM){    
    var nodes = Scatter.graphData().nodes;
    var metainfo = Scatter.graphData().meta[metaNM];
    for (var jj = 0; jj < nodes.length; jj++) {
        var n = nodes[jj];
        if (name === metainfo[jj]) {            
            colorNodeObj(n, "#bababa");
        }
    }
}

//Author: Zhiqiang Pang
function highlightCustdsetscolor(metasets){
    var count = 0;
    var metaData = Scatter.graphData().meta;
    var meta_table = Scatter.graphData().meta_table;
    var current_compri = $('#compariType').val();
    
    var GroupInfo = [];
    for (var mm = 0; mm < meta_table.NO.length; mm++) {
        var disVec = [], severiVec = [], outVec = [];
        if (meta_table.COVID[mm] !== 0)
            disVec.push("COVID");
        if (meta_table.nonCOVID[mm] !== 0)
            disVec.push("nonCOVID");
        if (meta_table.HC[mm] !== 0)
            disVec.push("HC");
        if (meta_table.Recovered[mm] !== 0)
            disVec.push("Recovered");
        if (meta_table.Survived[mm] !== 0)
            outVec.push("Survived");
        if (meta_table.Deceased[mm] !== 0)
            outVec.push("Deceased");
        if (meta_table.Asymptomatic[mm] !== 0)
            severiVec.push("Asymptomatic");
        if (meta_table.Mild_Moderate[mm] !== 0)
            severiVec.push("Mild_Moderate");
        if (meta_table.Severe[mm] !== 0)
            severiVec.push("Severe");
        if (meta_table.Critical[mm] !== 0)
            severiVec.push("Critical");        
        GroupInfo.push({DisVec: disVec, SerVec: severiVec, OutVec: outVec});
    }
    
    Scatter.graphData().nodes.forEach(function (n) {
        var condi1 = false, condi2 = false, condi3 = false, condi4 = false, condi5 = false, condi6 = false;
        if(metasets.Platform.includes(metaData.Platforms[count]) || 
                metasets.Platform.includes("All")){
            condi1 = true;
        }
        if (metasets.Blood.includes(metaData.BloodTypes[count]) || 
                metasets.Blood.includes("All")) {
            condi2 = true;
        }
        if (metasets.Polarity.includes(metaData.Polarities[count]) || 
                metasets.Polarity.includes("All")) {
            condi3 = true;
        }
        if (metasets.Country.includes(metaData.Countries[count]) || 
                metasets.Country.includes("All")) {
            condi4 = true;
        }
        if (metasets.Population.includes(metaData.Populations[count]) || 
                metasets.Population.includes("All")) {
            condi5 = true;
        }
        
        var datasetNM = metaData.Datasets[count];
        var orderval = meta_table.NO.indexOf(datasetNM);

        if (metasets.Comparison.includes("All")) {
            condi6 = true;
        } else {
            var comRES = metasets.Comparison.every(function (x) {
                if (current_compri === "diseases") {
                    return(GroupInfo[orderval].DisVec.includes(x));
                }
                if (current_compri === "severities") {
                    return(GroupInfo[orderval].SerVec.includes(x));
                }
                if (current_compri === "outcomes") {
                    return(GroupInfo[orderval].OutVec.includes(x));
                }
            });
            if (comRES) {
                condi6 = true;
            }
        }
        
        if(condi1 && condi2 && condi3 && condi4 && condi5 && condi6){
            colorNodeObj(n, "#0000FF");
        } else {
            colorNodeObj(n, "#bababa");
        }
        count++;
    });
    summarizeCutomizedData();
}

function highlightSelectedMeta_old(name, color, haloBool, encasingType) {
    var sizeval = parseInt($("#nsize").val());
    if (current_main_plot === "score") {
        var ids = [];
        Scatter.graphData().nodes.forEach(function (n) {

            if (n.metatype === name) {
                ids.push(n.id)
                n.highlight = 1
                n.colorb = color
                n.tcolor = color
                colorNodeObj(n, color)
                sizeNodeObj(n, sizeval / 2)
                if (haloBool) {
                    outlineNode(n, current_halo_color)
                } else {
                    deleteOutline(n)
                }
            }
        })
        Scatter.graphData().links.forEach(function (l) {
            colorLineObj2(l, l.color)
        })
        if (encasingType !== "na") {
            computeEncasing(ids, encasingType, current_encasing_color, current_module, 0.95, 0.5);
        } else {
            deleteMetaSphere(current_module, Scatter.scene());
        }
    } else {
        var sc = scene2
        sc.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (child.userData.nodeData !== undefined) {

                    var n = child.userData.nodeData;
                    if (n.metatype === name) {
                        n.highlight = 1
                        n.tcolor = color
                        colorNodeObjScene2(child, color)
                        if (haloBool) {
                            //outlineNode(child, color)
                        }
                    }
                }
            }
        });
    }
}

function unhighlightSelectedMeta_old(name) {
    if (current_main_plot === "score") {
        Scatter.graphData().nodes.forEach(function (n) {
            if (n.metatype === name) {
                n.highlight = 0
                if (n.tcolor === undefined) {
                    colorNodeObj(n, n.colorb)
                } else {
                    colorNodeObj(n, n.tcolor)
                }
                deleteOutline(n)
            }
        })
        Scatter.graphData().links.forEach(function (l) {
            colorLineObj2(l, l.color)
        })
    } else {
        var sc = scene2
        sc.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (child.userData.nodeData !== undefined) {

                    var n = child.userData.nodeData;
                    if (n.metatype === name) {
                        n.highlight = 1
                        colorNodeObjScene2(child, n.tcolor)
                    }
                }
            }
        });
    }
    var stats = $("#stats");
    stats.empty();
}

//Author: Zhiqiang Pang
function emptystatspanel(){
    $("#stats").empty();
}

//Author: Zhiqiang Pang
function displaySelectedMetaSum(thisitem, thismeta){
    //emptystatspanel();
    var statsct = $("#stats");
    statsct.empty();    
    var meta_table = gData.meta_table;
    
    if(thismeta === "Datasets"){
        for(var jj = 0; jj < meta_table.NO.length; jj++){            
            if(meta_table.NO[jj] === thisitem){
                statsct.append('<li><strong>Publication</strong>: ' + meta_table.Publication[jj] + '</li>');
                statsct.append('<li><strong>Platform</strong>: ' + meta_table.Platform[jj] + '</li>');
                statsct.append('<li><strong>IonMode</strong>: ' + meta_table.IonMode[jj] + '</li>');
                statsct.append('<li><strong>Country</strong>: ' + meta_table.Countries[jj] + '</li>');
                statsct.append('<li><strong>Wave</strong>: ' + meta_table.Waves[jj] + '</li>');
                statsct.append('<li><strong>Samples</strong>: <br></li>');
                if(meta_table.COVID[jj] !== 0){
                    statsct.append('COVID: ' + meta_table.COVID[jj] + '<br>');
                }
                if(meta_table.nonCOVID[jj] !== 0) {
                    statsct.append('nonCOVID: ' + meta_table.nonCOVID[jj] + '<br>');
                }
                if(meta_table.HC[jj] !== 0) {
                    statsct.append('HC: ' + meta_table.HC[jj] + '<br>');
                }
                if (meta_table.Recovered[jj] !== 0) {
                    statsct.append('Convalescent:' + meta_table.Recovered[jj] + '<br>');
                }
                if(meta_table.QC[jj] !== 0) {
                    statsct.append('QC: ' + meta_table.QC[jj] + '<br>');
                }                
                break;
            }
        }
    }
    
    if(thismeta === "Platforms") {        
        var desp = "------------ Details ------------<br>";
        var plabel; //platform label
        var mlabel;
        for (var jj = 0; jj < meta_table.Platform.length; jj++) {
            if(meta_table.Metabolomics[jj] === "Targeted" && meta_table.Platform[jj] === "LC-MS"){
                plabel = 'LC-MSt';
            } else if(meta_table.Metabolomics[jj] === "Untargeted" && meta_table.Platform[jj] === "LC-MS") {
                plabel = 'LC-MSu';
            } else {
                plabel = meta_table.Platform[jj];
                mlabel = meta_table.Metabolomics[jj];
            }
            if (plabel == thisitem) {                
                desp = desp + '<li>' + meta_table.NO[jj] + ': ' + meta_table.Publication[jj] + '</li>';                
            }
        }
        if(thisitem === "LC-MSu"){
            statsct.append('<li><strong>Platform</strong>: LC-MS (Untargeted)</li>');
        } else if(thisitem === "LC-MSt"){
            statsct.append('<li><strong>Platform</strong>: LC-MS (Targeted)</li>');
        } else {
            statsct.append('<li><strong>Platform</strong>: ' + thisitem + ' (' + mlabel + ')</li>');
        }
        statsct.append(desp);
    }
    
    if(thismeta === "BloodTypes") {
        var count = 0;
        for (var jj = 0; jj < meta_table.SampleTypes.length; jj++) {
            if (meta_table.SampleTypes[jj] === thisitem && 
                    meta_table.DataType[jj] !== "Compound List" &&
                    meta_table.DataType[jj] !== "Mass Table") {
                count++;
            }
        }
        statsct.append('A total of <strong>' + count + '</strong> datasets use ' + thisitem);        
    }
    
    if(thismeta === "Polarities") {
        var count = 0;
        for (var jj = 0; jj < meta_table.Extration.length; jj++) {
            if (meta_table.Extration[jj] === thisitem && 
                    meta_table.DataType[jj] !== "Compound List" &&
                    meta_table.DataType[jj] !== "Mass Table") {
                count++;
            }
        }
        if(thisitem === "Both") {
            statsct.append('A total of <strong>' + count + '</strong> datasets generated based on both hydrophilic and hydrophobic components of blood');        
        } else if(thisitem === "Lipid"){
            statsct.append('A total of <strong>' + count + '</strong> datasets generated based on hydrophobic (Lipid) components of blood');
        } else if(thisitem === "Polar"){
            statsct.append('A total of <strong>' + count + '</strong> datasets generated based on hydrophilic (Polar) components of blood');
        }
    }
    
    if(thismeta === "Populations"){
        var count = 0;
        for (var jj = 0; jj < meta_table.Population.length; jj++) {
            if (meta_table.Population[jj] === thisitem && 
                    meta_table.DataType[jj] !== "Compound List" &&
                    meta_table.DataType[jj] !== "Mass Table") {
                count++;
            }
        }
        if(thisitem === "All") {
            statsct.append('A total of <strong>' + count + '</strong> datasets included both children and adults');        
        } else if(thisitem === "Adults"){
            statsct.append('A total of <strong>' + count + '</strong> datasets included adults');
        } else if(thisitem === "Children"){
            statsct.append('A total of <strong>' + count + '</strong> dataset included children only');
        }
    }
    
    if (thismeta === "Countries") {
        var count = 0;
        for (var jj = 0; jj < meta_table.Countries.length; jj++) {
            if (meta_table.Countries[jj] === thisitem &&
                    meta_table.DataType[jj] !== "Compound List" &&
                    meta_table.DataType[jj] !== "Mass Table") {
                count++;
            }
        }
        statsct.append('<strong>' + count + '</strong> datasets are collected from <strong>' + thisitem + '</strong>');
    }
    
    if (thismeta === "Diseases") {
        var count = 0;
        for (var jj = 0; jj < meta_table[thisitem].length; jj++) {
            if (meta_table.DataType[jj] !== "Compound List" &&
                    meta_table.DataType[jj] !== "Mass Table") {
                count = count + meta_table[thisitem][jj];
            }
        }
        if(thisitem === "QC"){
            var moreannotation = " (Quality Control)";
        } else if (thisitem === "HC") {
            moreannotation = " (Healthy Control)";
        } else if(thisitem === 'nonCOVID') {
            moreannotation = " (patients showing similar symptoms of COVID-19, but tested as negative of SARS-COV-2 virus)";
        } else if(thisitem === 'Recovered'){
            moreannotation = " (patients recovered from COVID-19)";
        } else if(thisitem === 'COVID'){
            moreannotation = " (patients infected with SARS-COV-2)";
        } else {
            moreannotation = "";
        }
        statsct.append('<strong>' + count + '</strong> ' + thisitem + moreannotation + ' samples are selected');
    }
    
    if(thismeta === "Severities") {
        var count = 0;
        if(thisitem === "Unknown"){
            statsct.append('These samples do not include any information on severity of diseases.');
            return;
        }
        for (var jj = 0; jj < meta_table[thisitem].length; jj++) {
            if (meta_table.DataType[jj] !== "Compound List" &&
                    meta_table.DataType[jj] !== "Mass Table") {
                count = count + meta_table[thisitem][jj];
            }
        }
        if (thisitem === "Severe") {
            moreannotation = " (usually hosptalized and/or oxygen therapy needed)";
        } else if (thisitem === 'Critical') {
            moreannotation = " (monitored in Intensive Care Unit, ICU)";
        } else if (thisitem === 'Mild_Moderate') {
            moreannotation = " (showing symptoms but recovering from home or community clinics)";
        } else if (thisitem === 'Asymptomatic') {
            moreannotation = " (tested postive of SARS-COV-2 but no any symptoms)";
        } else {
            moreannotation = "";
        }
        statsct.append('<strong>' + count + '</strong> ' + thisitem + moreannotation + ' samples are selected');
    }
    
    if(thismeta === "OutComes") {
        var count = 0;
        if(thisitem === "Unknown"){
            statsct.append('These samples do not include any information on outcomes of diseases.');
            return;
        }
        for (var jj = 0; jj < meta_table[thisitem].length; jj++) {
            if (meta_table.DataType[jj] !== "Compound List" &&
                    meta_table.DataType[jj] !== "Mass Table") {
                count = count + meta_table[thisitem][jj];
            }
        }
        if (thisitem === "Survived") {
            moreannotation = " (Acute phase of COVID-19 at the sample collection time, but Survived finally)";
        } else if (thisitem === 'Deceased') {
            moreannotation = " (Acute phase of COVID-19 at the sample collection time, but failed to recover and deceased finally)";
        } else {
            moreannotation = "";
        }
        statsct.append('<strong>' + count + '</strong> ' + thisitem + moreannotation + ' samples are selected');
    }
}

//Author: Zhiqiang Pang
function summarizeCutomizedData(){
    emptystatspanel();
    var statsct = $("#stats");

    var platfromVec = metaSets.Platform;
    var countryVec = metaSets.Country;
    var bloodVec = metaSets.Blood;
    var polarityVec = metaSets.Polarity;
    var populationVec = metaSets.Population;
    var compariVec = metaSets.Comparison;
    
    var meta_table = Scatter.graphData().meta_table;
    var includedDataSets = [];
    var covidNum = 0, noncovidNum = 0, hcNum = 0, recoveredNum = 0, asympNum = 0, mmNum = 0, severeNum = 0, criticalNum = 0, deceasedNum = 0, survivedNum = 0;

    for(var i=0; i < meta_table.NO.length; i++){
        if((meta_table.DataType[i] === "Mass Table") || (meta_table.DataType[i] === "Compound List")) continue;
        
        var condi1 = false, condi2 = false, condi3 = false, condi4 = false, condi5 = false;
        var metabotype = '';
        if((meta_table.Metabolomics[i] === "Untargeted") && (meta_table.Platform[i] === "LC-MS")) {
            metabotype = 'u';
        } else if(meta_table.Platform[i] === "LC-MS") {
            metabotype = 't';
        } else {
            metabotype = '';
        };
        if(platfromVec.includes(meta_table.Platform[i] + metabotype) || platfromVec.includes("All")){
            condi1 = true;
        }
        if (countryVec.includes(meta_table.Countries[i]) || countryVec.includes("All")) {
            condi2 = true;
        }
        if (bloodVec.includes(meta_table.SampleTypes[i]) || bloodVec.includes("All")) {
            condi3 = true;
        }
        if (polarityVec.includes(meta_table.Extration[i]) || polarityVec.includes("All")) {
            condi4 = true;
        }
        if (populationVec.includes(meta_table.Population[i]) || populationVec.includes("All")) {            
            condi5 = true;
        }
        

        if(condi1 && condi2 && condi3 && condi4 && condi5) {            
            
            if((compariVec.includes("COVID")) && (meta_table.COVID[i] === 0)){continue;}
            if((compariVec.includes("nonCOVID")) && (meta_table.nonCOVID[i] === 0)){continue;}
            if((compariVec.includes("HC")) && (meta_table.HC[i] === 0)){continue;}
            if((compariVec.includes("Recovered")) && (meta_table.Recovered[i] === 0)){continue;}
            if((compariVec.includes("Asymptomatic")) && (meta_table.Asymptomatic[i] === 0)){continue;}
            if((compariVec.includes("Mild_Moderate")) && (meta_table.Mild_Moderate[i] === 0)){continue;}
            if((compariVec.includes("Severe")) && (meta_table.Severe[i] === 0)){continue;}
            if((compariVec.includes("Critical")) && (meta_table.Critical[i] === 0)){continue;}
            if((compariVec.includes("Deceased")) && (meta_table.Deceased[i] === 0)){continue;}
            if((compariVec.includes("Survived")) && (meta_table.Survived[i] === 0)){continue;}
            
            includedDataSets.push(meta_table.NO[i]);
            var allcompBool = compariVec.includes("All");
            if(compariVec.includes("COVID") || allcompBool) covidNum = covidNum + meta_table.COVID[i];            
            if(compariVec.includes("nonCOVID") || allcompBool) noncovidNum = noncovidNum + meta_table.nonCOVID[i];
            if(compariVec.includes("HC") || allcompBool) hcNum = hcNum + meta_table.HC[i];
            if(compariVec.includes("Recovered") || allcompBool) recoveredNum = recoveredNum + meta_table.Recovered[i];
            if(compariVec.includes("Asymptomatic") || allcompBool) asympNum = asympNum + meta_table.Asymptomatic[i];
            if(compariVec.includes("Mild_Moderate") || allcompBool) mmNum = mmNum + meta_table.Mild_Moderate[i];
            if(compariVec.includes("Severe") || allcompBool) severeNum = severeNum + meta_table.Severe[i];
            if(compariVec.includes("Critical") || allcompBool) criticalNum = criticalNum + meta_table.Critical[i];
            if(compariVec.includes("Deceased") || allcompBool) deceasedNum = deceasedNum + meta_table.Deceased[i];
            if(compariVec.includes("Survived") || allcompBool) survivedNum = survivedNum + meta_table.Survived[i];
        }
    }
    
    if (includedDataSets.length > 0) {
        statsct.append('A total of <strong>' + includedDataSets.length + '</strong> datasets found!<br>');
        statsct.append('----------- Details -----------<br>');

        var sum = 'Total sample number of different groups: <br>';
        if ($('#compariType').val() === 'diseases') {
            if(covidNum !== 0) sum = sum + '<li><strong>COVID</strong>: ' + covidNum + '</li>';
            if(noncovidNum !== 0) sum = sum + '<li><strong>nonCOVID</strong>: ' + noncovidNum + '</li>';
            if(hcNum !== 0) sum = sum + '<li><strong>HC</strong>: ' + hcNum + '</li>';
            if(recoveredNum !== 0) sum = sum + '<li><strong>Recovered</strong>: ' + recoveredNum + '</li>';
        } else if($('#compariType').val() === 'severities') {
            if(asympNum !== 0) sum = sum + '<li><strong>Asymptomatic</strong>: ' + asympNum + '</li>';
            if(mmNum !== 0) sum = sum + '<li><strong>Mild_Moderate</strong>: ' + mmNum + '</li>';
            if(severeNum !== 0) sum = sum + '<li><strong>Severe</strong>: ' + severeNum + '</li>';
            if(criticalNum !== 0) sum = sum + '<li><strong>Critical</strong>: ' + criticalNum + '</li>';
        } else if($('#compariType').val() === 'outcomes'){
            if(deceasedNum !== 0) sum = sum + '<li><strong>Deceased</strong>: ' + deceasedNum + '</li>';
            if(survivedNum !== 0) sum = sum + '<li><strong>Survived</strong>: ' + survivedNum + '</li>';
        } else {
            //This happens when user select 'None' --> which shows all information
            if(covidNum !== 0) sum = sum + '<li><strong>COVID</strong>: ' + covidNum + '</li>';
            if(noncovidNum !== 0) sum = sum + '<li><strong>nonCOVID</strong>: ' + noncovidNum + '</li>';
            if(hcNum !== 0) sum = sum + '<li><strong>HC</strong>: ' + hcNum + '</li>';
            if(recoveredNum !== 0) sum = sum + '<li><strong>Recovered</strong>: ' + recoveredNum + '</li>';
            if(deceasedNum !== 0) sum = sum + '<li><strong>Deceased</strong>: ' + deceasedNum + '</li>';
            if(survivedNum !== 0) sum = sum + '<li><strong>Survived</strong>: ' + survivedNum + '</li>';
            if(asympNum !== 0) sum = sum + '<li><strong>Asymptomatic</strong>: ' + asympNum + '</li>';
            if(mmNum !== 0) sum = sum + '<li><strong>Mild_Moderate</strong>: ' + mmNum + '</li>';
            if(severeNum !== 0) sum = sum + '<li><strong>Severe</strong>: ' + severeNum + '</li>';
            if(criticalNum !== 0) sum = sum + '<li><strong>Critical</strong>: ' + criticalNum + '</li>';
        }

        statsct.append(sum);
        
    } else {
        statsct.append('No dataset meet the criteria!');
    }   
    
}

//Author: Zhiqiang Pang
function organizeConfirmData(){
    
    var platfromVec = metaSets.Platform;
    var countryVec = metaSets.Country;
    var bloodVec = metaSets.Blood;
    var polarityVec = metaSets.Polarity;
    var populationVec = metaSets.Population;
    var compariVec = metaSets.Comparison;
    
    var meta_table = Scatter.graphData().meta_table;
    var includedDataSets = [];
    var sizeSums = [];
    var cmpdSums = [];
    
    for(var i=0; i < meta_table.NO.length; i++){
        if((meta_table.DataType[i] === "Mass Table") || (meta_table.DataType[i] === "Compound List")) continue;        
        
        var condi1 = false, condi2 = false, condi3 = false, condi4 = false, condi5 = false;
        var metabotype = '';
        if(meta_table.Metabolomics[i] === "Untargeted") {metabotype = 'u';} else {metabotype = 't';};
        if(platfromVec.includes(meta_table.Platform[i] + metabotype) || platfromVec.includes("All")){
            condi1 = true;
        }
        if (countryVec.includes(meta_table.Countries[i]) || countryVec.includes("All")) {
            condi2 = true;
        }
        if (bloodVec.includes(meta_table.SampleTypes[i]) || bloodVec.includes("All")) {
            condi3 = true;
        }
        if (polarityVec.includes(meta_table.Extration[i]) || polarityVec.includes("All")) {
            condi4 = true;
        }
        if (populationVec.includes(meta_table.Population[i]) || populationVec.includes("All")) {            
            condi5 = true;
        }

        if(condi1 && condi2 && condi3 && condi4 && condi5) {
            
            if((compariVec.includes("COVID")) && (meta_table.COVID[i] === 0)){continue;}
            if((compariVec.includes("nonCOVID")) && (meta_table.nonCOVID[i] === 0)){continue;}
            if((compariVec.includes("HC")) && (meta_table.HC[i] === 0)){continue;}
            if((compariVec.includes("Recovered")) && (meta_table.Recovered[i] === 0)){continue;}
            if((compariVec.includes("Asymptomatic")) && (meta_table.Asymptomatic[i] === 0)){continue;}
            if((compariVec.includes("Mild_Moderate")) && (meta_table.Mild_Moderate[i] === 0)){continue;}
            if((compariVec.includes("Severe")) && (meta_table.Severe[i] === 0)){continue;}
            if((compariVec.includes("Critical")) && (meta_table.Critical[i] === 0)){continue;}
            if((compariVec.includes("Deceased")) && (meta_table.Deceased[i] === 0)){continue;}
            if((compariVec.includes("Survived")) && (meta_table.Survived[i] === 0)){continue;}
            
            includedDataSets.push(meta_table.NO[i]);
            var sum = '';
            if((compariVec.includes("COVID") || compariVec.includes("All")) && (meta_table.COVID[i] !== 0))
            {sum = sum + 'COVID(' + meta_table.COVID[i] + '); ';}
            if((compariVec.includes("nonCOVID") || compariVec.includes("All")) && (meta_table.nonCOVID[i] !== 0)) 
            {sum = sum + 'nonCOVID(' + meta_table.nonCOVID[i] + '); ';}
            if((compariVec.includes("HC") || compariVec.includes("All")) && (meta_table.HC[i] !== 0)) 
            {sum = sum + 'HC(' + meta_table.HC[i] + '); ';}
            if((compariVec.includes("Recovered") || compariVec.includes("All")) && (meta_table.Recovered[i] !== 0)) 
            {sum = sum + 'Recovered(' + meta_table.Recovered[i] + '); ';}
            if((compariVec.includes("Asymptomatic") || compariVec.includes("All")) && (meta_table.Asymptomatic[i] !== 0))
            {sum = sum + 'Asymptomatic(' + meta_table.Asymptomatic[i] + '); ';}
            if((compariVec.includes("Mild_Moderate") || compariVec.includes("All")) && (meta_table.Mild_Moderate[i] !== 0))
            {sum = sum + 'Mild_Moderate(' + meta_table.Mild_Moderate[i] + '); ';}
            if((compariVec.includes("Severe") || compariVec.includes("All")) && (meta_table.Severe[i] !== 0))
            {sum = sum + 'Severe(' + meta_table.Severe[i] + '); ';}
            if((compariVec.includes("Critical") || compariVec.includes("All")) && (meta_table.Critical[i] !==0))
            {sum = sum + 'Critical(' + meta_table.Critical[i] + '); ';}
            if((compariVec.includes("Deceased") || compariVec.includes("All")) && (meta_table.Deceased[i] !==0))
            {sum = sum + 'Deceased(' + meta_table.Deceased[i] + '); ';}
            if((compariVec.includes("Survived") || compariVec.includes("All")) && (meta_table.Survived[i] !==0))
            {sum = sum + 'Survived(' + meta_table.Survived[i] + '); ';}
            sizeSums.push(sum);
            cmpdSums.push(meta_table.CompoundNum[i]);
        }
    }
    return {DataSets: includedDataSets, Size: sizeSums, CMPD: cmpdSums};    
}

//Author: Zhiqiang Pang
function activateAnalysisPanel() {
    $('#confirmdlg').dialog('close');
    $('#bd').layout('remove', 'east');
    
    var options = {
        id: 'module',
        region: 'east',
        width: 265,
        split: true,
        title: 'Formal Analysis'
    };
    $('#bd').layout('add', options);   

    $("#module").append(
            '<div id="module">' +
            '<p style= "font-size:15px; padding-left: 15px; margin-top: 8.5px; color: #003972; font-weight: bold;">Selec a Module for further analysis.</p>' +
            
            '<input style = "margin-left:15px" type="radio" id="statsx" value="statsx" name = "modulex">' +
            '<label style = "font-size:14.5px; color: #003972;" for="statsx">Statistical Analysis</label><br>' +
            '<div style = "margin-left: 20px; margin-right: 12px; margin-top: 12px; border: dashed grey thin; border-radius: 5px">' + 
            '<p style = "margin: 6px;">"<b>Statistical Analysis</b>" Module includes common regular statatical methods (e.g. Univariate Analysis, Multivariate Analysis and Clustering Analysis).</p></div><br/>' + 
            
            '<input style = "margin-left:15px" type="radio" id="markerx" value="markerx" name = "modulex">' +
            '<label style = "font-size:14.5px; color: #003972" for="markerx">Biomarker Analysis</label><br>' +
            '<div style = "margin-left: 20px; margin-right: 12px; margin-top: 12px; border: dashed grey thin; border-radius: 5px">' + 
            '<p style = "margin: 6px;">"<b>Biomarker Analysis</b>" Module is used to evaluate the accuracy and precision of compound(s) to work as biomarker(s) with multiple approaches. </p></div><br/>' + 
                        
            '<input style = "margin-left:15px" type="radio" id="metax" value="metax" name = "modulex">' +
            '<label style = "font-size:14.5px; color: #003972" for="metax">Meta-Analysis</label><br>' +
            '<div style = "margin-left: 20px; margin-right: 12px; margin-top: 12px; border: dashed grey thin; border-radius: 5px">' + 
            '<p style = "margin: 6px;">"<b>Meta-Analysis</b>" Module is used to integrate multiple comparable and independent datasets for more robust biomarkers with increased confidence.</p></div><br/>' + 
                        
            '<input style = "margin-left:15px" type="radio" id="pathx" value="pathx" name = "modulex">' +
            '<label style = "font-size:14.5px; color: #003972" for="pathx">Pathway Analysis</label><br>' +
            '<div style = "margin-left: 20px; margin-right: 12px; margin-top: 12px; border: dashed grey thin; border-radius: 5px">' + 
            '<p style = "margin: 6px;">"<b>Pathway Analysis</b>" Module can be used for analyzing functional difference from pathway level (with considering the topological effect).</p></div><br/>' + 

            '<input style = "margin-left:15px" type="radio" id="enrichx" value="enrichx" name = "modulex">' +
            '<label style = "font-size:14.5px; color: #003972" for="enrichx">Enrichment Analysis</label><br>' +
            '<div style = "margin-left: 20px; margin-right: 12px; margin-top: 12px; border: dashed grey thin; border-radius: 5px">' + 
            '<p style = "margin: 6px;">"<b>Enrichment Analysis</b>" Module is designed to perform the functional enrichment towards different databases (without including topological effect).</p></div><br/>' + 
            
            '<input style = "margin-left:15px" type="radio" id="netx" value="netx" name = "modulex">' +
            '<label style = "font-size:14.5px; color: #003972" for="netx">Network Analysis</label><br>' +
            '<div style = "margin-left: 20px; margin-right: 12px; margin-top: 12px; border: dashed grey thin; border-radius: 5px">' + 
            '<p style = "margin: 6px;">"<b>Network Analysis</b>" Module is used to do analysis and visulization from global network level. The interaction among compounds could also be explored.</p></div><br/>' + 
            
            '<div style = "text-align: center; margin-top: 20px">'+
            '<button style = "font-size:11px" type="button" onclick="ProceedToModule()">Proceed</button>' + 
            '<button style = "font-size:11px; margin-left: 20px;" type="button" onclick="CancelToModule()">Cancel</button>' +
            '</div>' +
            
            '</div>'
            );
}

//Author: Zhiqiang Pang
function ProceedToModule(){
    if(finalSets.length> 1 && !$("#metax").prop("checked")) {
        $.messager.alert('Error', "Multiple dataset cannot be analyzed with this module! They could ONLY be used for meta-analysis!", 'error');
        return;
    }
    
    if(metaSets.Comparison.length > 2 && ($("#markerx").prop("checked") || $("#pathx").prop("checked") || $("#enrichx").prop("checked") || $("#netx").prop("checked") || $("#metax").prop("checked"))){
        $.messager.alert('Error', "More than two groups are ONLY allowed for <u><b>Statistical Analysis</b></u> module!", 'error');
        return;
    }
    
    if($("#statsx").prop("checked")) {
        gotoModule("statsx");
    } else if($("#markerx").prop("checked")){
        gotoModule("markerx");
    } else if($("#metax").prop("checked")){
        gotoModule("metax");
    } else if($("#pathx").prop("checked")){
        gotoModule("pathx");
    } else if($("#enrichx").prop("checked")){
        gotoModule("enrichx");
    } else if($("#netx").prop("checked")){
        gotoModule("netx");
    }
}

//Author: Zhiqiang Pang
function gotoModule(Module){
    console.log("Going to this module --> " + Module);
    if(Module === "statsx" || Module === "markerx" || Module === "metax" || Module === "pathx" || Module === "enrichx" || Module === "netx"){
        var ComparisonVec = metaSets.Comparison.toString();
        var dts = finalSets.toString();
        $.ajax({
            beforeSend: function () {
            },
            dataType: "html",
            type: "POST",
            url: '/MetaboAnalyst/faces/AjaxCall',
            data: {function: 'gofromCOVID', module: Module, datasets: dts, comparis: ComparisonVec},
            async: false,
            success: function (result) {                
                if(result === "ERROR:MultipleGroupsNotAllowed!"){
                    $.messager.alert('Error', "More than two groups are NOT allowed for this module!", 'error');
                } else if(result === "ERROR:NOenoughComoundInsected") {
                    $.messager.alert('Error', "No enough intersected compounds found in the datasets you have selected!", 'error');
                } else if(result !== "null"){
                    window.parent.location.href = result;
                }                
            }
        });
  
    } else {
        $.messager.alert('Error', "Weird Module selected!", 'error');
    }
}

//Author: Zhiqiang Pang
function CancelToModule(){
    finalSets = [];
    $('#bd').layout('remove', 'east');
}

//Author: Zhiqiang Pang
function modifyNode(node) {
    node.unhoveredColor = highlightColor;
    unoutlineAllNode();
    highlightNode(node);
    displayNodeInfo(node);
}

//Author: Zhiqiang Pang
function unoutlineAllNode(){    
    Scatter.graphData().nodes.forEach(function (n) {
        if (n.outline === 1) {
            deleteOutline(n);
        }
    });    
}

//Author: Zhiqiang Pang
function outlineNode(n, color) {
    var nsize;
    if (n.outline === 1) {
        deleteOutline(n);
    }
    n.outline = 1;
    var colorValue = parseInt(n.color.replace("#", "0x"), 16);
    var colored = new THREE.Color(colorValue);
    var particle = new THREE.Sprite(new THREE.SpriteMaterial({
        map: new THREE.CanvasTexture(generateSprite(color)),
        alphaTest: 0.5,
        transparent: true,
        fog: true
    }));
    particle.scale.x = particle.scale.y = n.__threeObj.userData.scaling[0] * 3 + 70;
    var jsonKey = n.id;
    outlineUIs[jsonKey] = particle;
    outlineUIs[jsonKey].userData.type = "halo";
    outlineUIs[jsonKey].userData.id = n.id;
    outlineUIs[jsonKey].userData.color = color.replace("#", "0x");
    //particle.position.set(threeNodes[n.id].position.x, threeNodes[n.id].position.y, threeNodes[n.id].position.z)
    var obj = n.__threeObj;
    obj.add(particle);
}

//Author: Zhiqiang Pang
function highlightNode(node) {
    node.highlight = 1;
    if (highlightType === "mixed") {
        colorNodeObj(node, highlightColor)
        //sizeNodeObj(node, node.size+2)
        //node.size = node.size + 2;
        return;
    } else if (highlightType === "node") {
        colorNodeObj(node, highlightColor)
        //sizeNodeObj(node, node.size+2)
        //node.size = node.size + 2;
    } else {
        if (outlineUIs[node.id] === null || outlineUIs[node.id] === undefined) {
            outlineNode(node, highlightColor);
        } else {
            deleteOutline(node);
            outlineNode(node, highlightColor);
        }
    }
}

//Author: Zhiqiang Pang
function displayNodeInfo(node) {
    var stats = $("#stats");
    stats.empty();

    stats.append('<li class="stats"><strong>Sample Name:</strong> ' + node.label + '</li>');
    stats.append('<li class="stats"><strong>Dataset Source:</strong> ' + node.metatype + '</li>');
    for(var i = 0; i < gData.nodes.length; i++){
        if(gData.nodes[i].label === node.label){
            break;
        }
    }

    stats.append('<p style="margin:5px">-------<strong>Metadata Summary</strong>-------</p>');
    stats.append('<p style="margin:2px"><strong>Blood Type:</strong> ' + gData.meta.BloodTypes[i] + '</p>');
    stats.append('<p style="margin:2px"><strong>Country:</strong> ' + gData.meta.Countries[i] + '</p>');    
    stats.append('<p style="margin:2px"><strong>Platform:</strong> ' + gData.meta.Platforms[i] + '</p>');
    stats.append('<p style="margin:2px"><strong>Polarity:</strong> ' + gData.meta.Polarities[i] + '</p>');
    stats.append('<p style="margin:2px"><strong>Age Group:</strong> ' + gData.meta.Populations[i] + '</p>');    
    stats.append('<p style="margin:2px"><strong>Disease:</strong> ' + gData.meta.Diseases[i] + '</p>');
    if(gData.meta.OutComes[i] !== null){
        stats.append('<p style="margin:2px"><strong>OutCome:</strong> ' + gData.meta.OutComes[i] + '</p>');
    }
    if(gData.meta.Severities[i] !== null){
        stats.append('<p style="margin:2px"><strong>Severities:</strong> ' + gData.meta.Severities[i] + '</p>');
    }    
}

//Author: Zhiqiang Pang
function openModuleColorPicker(ev, row) {
    current_module = ev.id;
    var inx = parseInt(ev.id.replace("meta_", "").slice(-1));    

    var row = $('#metadg').datagrid('getRows')[inx];
    current_selected_row = row;
    current_point_color = $("#" + current_module).css("backgroundColor");
    var a = current_point_color.split("(")[1].split(")")[0];
    a = a.split(",");

    current_point_color = "#" + convertToHex(a);
    document.getElementById("moduleColTxt").innerHTML = "Customize color of " + current_selected_row.name + ": ";        
    $("#custommeta").spectrum("set", current_point_color);
    $('#modulecolordlg').dialog('open');
}

//Author: Zhiqiang Pang
function changeMetaColor(){
    var thiscol = $("#custommeta").spectrum("get").toHexString();
    var thismeta = curr_meta;
    var thissets = current_selected_row.name;
    
    for (var i = 0; i < gData.meta.BloodTypes.length; i++){
        if((thismeta === "Datasets") && (gData.meta.Datasets[i] === thissets)){            
            colorNodeObj(gData.nodes[i], thiscol);
            gData.nodes[i].colorb = thiscol;
            continue;
        }
        if ((thismeta === "Platforms") && (gData.meta.Platforms[i] === thissets)) {
            colorNodeObj(gData.nodes[i], thiscol);
            gData.nodes[i].colorb = thiscol;
            continue;
        }
        if ((thismeta === "BloodTypes") && (gData.meta.BloodTypes[i] === thissets)) {
            colorNodeObj(gData.nodes[i], thiscol);
            gData.nodes[i].colorb = thiscol;
            continue;
        }
        if ((thismeta === "Polarities") && (gData.meta.Polarities[i] === thissets)) {
            colorNodeObj(gData.nodes[i], thiscol);
            gData.nodes[i].colorb = thiscol;
            continue;
        }
        if ((thismeta === "Populations") && (gData.meta.Populations[i] === thissets)) {
            colorNodeObj(gData.nodes[i], thiscol);
            gData.nodes[i].colorb = thiscol;
            continue;
        }
        if ((thismeta === "Countries") && (gData.meta.Countries[i] === thissets)) {
            colorNodeObj(gData.nodes[i], thiscol);
            gData.nodes[i].colorb = thiscol;
            continue;
        }
        if ((thismeta === "OutComes") && (gData.meta.OutComes[i] === thissets)) {
            colorNodeObj(gData.nodes[i], thiscol);
            gData.nodes[i].colorb = thiscol;
            continue;
        }
        if ((thismeta === "Severities") && (gData.meta.Severities[i] === thissets)) {
            colorNodeObj(gData.nodes[i], thiscol);
            gData.nodes[i].colorb = thiscol;
            continue;
        }
        if ((thismeta === "Diseases") && (gData.meta.Diseases[i] === thissets)) {
            colorNodeObj(gData.nodes[i], thiscol);
            gData.nodes[i].colorb = thiscol;
            continue;
        }
    }
    var inx = parseInt(current_module.replace("meta_", ""));
    var newrow = {
        index: inx,
        name: current_selected_row.name,
        size: current_selected_row.size,
        color: '<span id=\"meta_' + inx + '" style="background-color:' + thiscol + '" onclick="openModuleColorPicker(this);event.stopPropagation()">&emsp;&emsp;&emsp;</span>'
    };
    
    meta_arr[inx] = newrow;
    $('#metadg').datagrid('loadData', meta_arr);
    $('#modulecolordlg').dialog('close');
}

//Author: Zhiqiang Pang
function resetNetwork() {
    bgColor = "#ffffff";
    labelColOpt = "#222222";
    changeBackground("#ffffff", "#ffffff");

    Scatter.cameraPosition(
            {x: initRadius * 1.2, y: initRadius * 1.2, z: initRadius * 1.7}, // new position
            {x: 0, y: 0, z: 0}, // lookAt ({ x, y, z })
            2000  // ms transition duration
            );
    Scatter.controls().target.set(0, 0, 0);

    Scatter.graphData().nodes.forEach(function (n) {
        n.highlight = 0;
        colorNodeObj(n, n.tcolor);
        opaNodeObj(n, 0.8);
        if (n.meta !== "mcia.seg") {
            sizeNodeObj(n, 1);
        }
        deleteOutline(n);
    });

    Scatter.graphData().links.forEach(function (l) {
        l.highlight = 0;
        l.width = 0;
        colorLineObj2(l, edgeColor);
        sizeLineObj(l, 0);
    });

    Scatter.scene().traverse(function (child) {
        if (child instanceof THREE.Mesh && typeof child.userData === "string") {
            if (child.userData.includes("fatline")) {
                child.visible = false;
            }
        }
    });
    if (gData.navigation !== "NA") {
        scene2.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (child.userData.nodeData !== undefined) {
                    var colorValue = parseInt(child.userData.nodeData.tcolor.replace("#", "0x"), 16);
                    var col = new THREE.Color(colorValue);
                    child.userData.nodeData.highlight = 0;
                    child.material.color = col;
                    child.material.opacity = 0.8;

                }
            }
        });
    }
    restoreMetaTable();
}

//Author: Zhiqiang Pang
function restoreMetaTable() {
    initTable();
    loadMeta();
}

//Author: Zhiqiang Pang
function pagerFilter(data) {
    if (typeof data.length === 'number' && typeof data.splice === 'function') {    // is array
        data = {
            total: data.length,
            rows: data
        };
    }
    var dg = $('#metadg');
    var opts = dg.datagrid('options');
    var pager = dg.datagrid('getPager');

    pager.pagination({
        showPageList: false,
        showRefresh: false,
        displayMsg: "",
        onSelectPage: function (pageNum, pageSize) {
            opts.pageNumber = pageNum;
            opts.pageSize = pageSize;
            pager.pagination('refresh', {
                pageNumber: pageNum,
                pageSize: pageSize
            });
            dg.datagrid('loadData', data);
        }
    });
    if (!data.originalRows) {
        data.originalRows = (data.rows);
    }
    var start = (opts.pageNumber - 1) * parseInt(opts.pageSize);
    var end = start + parseInt(opts.pageSize);
    data.rows = (data.originalRows.slice(start, end));
    return data;
}


// -------------- ================== --- Splitter --- =================------------------//

// Following section are old functions from omicsanalyst, will be organized and cleaned later

var tempV = new THREE.Vector3();
var lpos = new THREE.Vector3();
var i_animation = 0;
function updateLabelPositions() {
    if (explodingBool == "abc") {
        while (i_animation < 1000) {
        }
        i_animation++;

        explodingBool = false;
    }
    if (!hideLabel) {
        var propertyNms;
        if (htmlTextBool) {
            propertyNms = Object.keys(spriteys)
        } else {
            propertyNms = Object.keys(spriteysCanvas)
        }

        var nodes = Scatter.graphData().nodes;
        var links = Scatter.graphData().links;
        var camera = Scatter.camera()
        camera.updateMatrix();
        camera.updateMatrixWorld();
        var frustum = new THREE.Frustum();
        frustum.setFromMatrix(new THREE.Matrix4().multiplyMatrices(camera.projectionMatrix, camera.matrixWorldInverse));

        nodes.forEach(function (n) {
            if (propertyNms.indexOf(n.id) !== -1 && n.__threeObj !== undefined) {
                // get the position of the center of the cube
                var sphere = n.__threeObj
                var distance = sphere.position.distanceTo(camera.position);
                sphere.updateWorldMatrix(true, false);
                sphere.getWorldPosition(tempV);
                tempV.project(camera);

                // convert the normalized position to CSS coordinates
                const x = (tempV.x * .5 + .5) * document.getElementById("3d-graph").clientWidth;
                const y = (tempV.y * -.5 + .5) * document.getElementById("3d-graph").clientHeight;

                // move the elem to that position
                var elem = spriteys[n.id]
                if (elem !== undefined && elem !== null) {
                    var textSprite = spriteysCanvas[n.id]
                    if (frustum.containsPoint(sphere.position)) {

                        if (htmlTextBool) {
                            elem.style.display = "block"
                            textSprite.visible = false
                        } else {
                            elem.style.display = "none"
                            textSprite.visible = true
                        }
                        if (htmlTextBool) {

                            elem.style.transform = `translate(-50%, -50%) translate(${x}px,${y}px)`;
                            var size = 1000 / distance * 100
                            if (size > 120) {
                                elem.style.fontSize = 120 + "%";
                            } else {
                                if (size < 60) {
                                    elem.style.display = "none"
                                } else {
                                    elem.style.fontSize = size + "%";
                                    var rsize = rescale2Range(size, 60, 120, 0, 100)
                                    elem.style.color = labelColOpt//'#' + labelColSpectrum.colourAt(rsize);
                                }
                            }
                        }


                        if (animationVal < 601) {
                            textSprite.visible = true
                            elem.style.display = "none"
                        }
                    } else {
                        elem.style.display = "none"
                    }
                }

            }
        })
        updateLabel()
    }
}

var hoveredNode;
function hoverNode(node, prevNode) {
    if (hoveredNode && hoveredNode.__threeObj && hoveredNode.unhoveredColor) {
        if (hoveredNode.__threeObj.children[0]) {
            if (highlightType !== "halo") {
                colorNodeObj(hoveredNode, hoveredNode.unhoveredColor)
            }
        }
    }

    if (!node) {
        return;
    }

    if (node.focused === undefined) {
        return;
    }

    if (node.moduleInx !== undefined) {
        initPos = {x: node.position.x, y: node.position.y, z: node.position.z}
        Scatter.graphData().nodes.forEach(function (n) {
            n.initPos = {x: n.x, y: n.y, z: n.z}
        })
        var propertyNms = Object.keys(boundingClusters)
        for (var i = 0; i < propertyNms.length; i++) {
            var sphe = boundingClusters[propertyNms[i]];
            var sphe1 = boundingClustersMesh[propertyNms[i]];
            sphe.initPos = {x: sphe.position.x, y: sphe.position.y, z: sphe.position.z}
            sphe1.initPos = {x: sphe.position.x, y: sphe.position.y, z: sphe.position.z}
        }
        return;
    }

    if (node.__threeObj) {
        if (node.__threeObj.children[0]) {
            initPos = {x: node.x, y: node.y, z: node.z}
            Scatter.graphData().nodes.forEach(function (n) {
                n.initPos = {x: n.x, y: n.y, z: n.z}
            })
            var propertyNms = Object.keys(boundingClusters)
            for (var i = 0; i < propertyNms.length; i++) {
                var sphe = boundingClusters[propertyNms[i]];
                sphe.initPos = {x: sphe.position.x, y: sphe.position.y, z: sphe.position.z}
                var sphe1 = boundingClustersMesh[propertyNms[i]];
                sphe1.initPos = {x: sphe1.position.x, y: sphe1.position.y, z: sphe1.position.z}
            }
            var newcol = LightenDarkenColor(node.color, -60)

            if (node.highlight !== 1) {
                node.unhoveredColor = node.color
                colorNodeObj(node, newcol)
            }
            hoveredNode = node


        }
    }
}

var vec = new THREE.Vector3();
function updateLabel() {

    var props = Object.keys(myTexts)

    if (props.length > 0) {
        for (var i = 0; i < props.length; i++) {
            var scaleVector = new THREE.Vector3();

            var sprite = myTexts[props[i]];
            var camera = Scatter.camera()
            var lengthFac = sprite.userData.id.length / 6
            if (props[i].includes(scene2.uuid)) {
                var scaleFactor = 0.10;
                sprite.scale.x = lengthFac * scaleFactor * vec.setFromMatrixPosition(sprite.matrixWorld).sub(camera.position).length();
                sprite.scale.y = 0.030 * vec.setFromMatrixPosition(sprite.matrixWorld).sub(camera.position).length();
                sprite.scale.z = 1
            } else {
                var scaleFactor = 0.06;
                sprite.scale.x = lengthFac * scaleFactor * vec.setFromMatrixPosition(sprite.matrixWorld).sub(camera.position).length();
                sprite.scale.y = 0.020 * vec.setFromMatrixPosition(sprite.matrixWorld).sub(camera.position).length();
                sprite.scale.z = 1
            }
        }
    }

    if (Scatter !== "abc") {
        var nodes = Scatter.graphData().nodes
        for (var i = 0; i < nodes.length; i++) {
            var scaleVector = new THREE.Vector3();
            var scaleFactor = 0.0014;
            var sprite = nodes[i].__threeObj;
            var camera = Scatter.camera()
            if (sprite !== undefined) {
                if (vec.setFromMatrixPosition(sprite.matrixWorld).sub(camera.position).length() < 1000) {
                    sprite.scale.x = sprite.userData.scaling[0] * 0.001 * vec.setFromMatrixPosition(sprite.matrixWorld).sub(camera.position).length();
                    sprite.scale.y = sprite.userData.scaling[1] * 0.001 * vec.setFromMatrixPosition(sprite.matrixWorld).sub(camera.position).length();
                    sprite.scale.z = sprite.userData.scaling[2] * 0.001 * vec.setFromMatrixPosition(sprite.matrixWorld).sub(camera.position).length();
                } else {
                    sprite.scale.x = sprite.userData.scaling[0]
                    sprite.scale.y = sprite.userData.scaling[1]
                    sprite.scale.z = sprite.userData.scaling[2]
                }
            }
        }
        var zeroVec = v(0, 0, 0)
        Scatter.controls.panSpeed = zeroVec.sub(camera.position).length() / 250000
    }
}

function updateColor() {
    setColorOpts(current_color_attr);
}

function highlightFunEnrichNodes(nodes, title, drawBorder) {

    if (nodes.length === 1) {
        searchNetwork(nodes[0]);
    } else {
        var nodeVec = [];
        highlight_mode = 1;
        if (current_main_plot === "score") {
            scene2.traverse(function (child) {
                if (child instanceof THREE.Mesh) {
                    if (child.userData.nodeData !== undefined) {

                        var id = child.userData.nodeData.id.split("_")[0]
                        var id2 = child.userData.nodeData.label.split("_")[0]

                        if (nodes.indexOf(id) !== -1 || nodes.indexOf(id2) !== -1) {
                            var colorValue = parseInt(highlightColor.replace("#", "0x"), 16);
                            var col = new THREE.Color(colorValue);
                            child.material.color = col;
                            child.userData.nodeData.highlight = 1
                            child.material.opacity = 0.8;
                            child.visible = true;
                            nodeVec.push({id: child.userData.nodeData.id, label: child.userData.nodeData.label});
                        } else {
                            if (child.userData.nodeData.highlight !== 1) {
                                child.material.transparent = true;
                                child.material.opacity = 0.15;
                                var colorValue = parseInt("#d3d3d3".replace("#", "0x"), 16);
                                var col = new THREE.Color(colorValue);
                                child.material.color = col;
                            }
                        }
                    }
                }
            });
        } else {
            Scatter.graphData().nodes.forEach(function (n) {
                if (nodes.indexOf(n.id) !== -1 || nodes.indexOf(n.label) !== -1) {
                    if (n.highlight !== 1) {
                        if (highlightType === "mixed") {
                            n.prevCol = n.color;
                            colorNodeObj(n, highlightColor)
                            opaNodeObj(n, 0.8)
                            n.highlight = 1;
                        } else {
                            highlightNode(n);
                            opaNodeObj(n, 1)
                            n.highlight = 1;
                        }
                    }
                    nodeVec.push({id: n.id, label: n.label});

                } else {
                    if (n.highlight !== 1) {
                        n.prevCol = n.color;
                        //colorNodeObj(n, "#d3d3d3")
                        opaNodeObj(n, 0.2)
                    }
                }
            });
        }

        displayCurrentSelectedNodes(nodeVec, title);
    }
}

function highlightDirectLinks() {
    Scatter.graphData().links.forEach(function (l) {
        if (l.source.highlight === 1 && l.target.highlight === 1) {
            l.width = 3;
            sizeLineObj(l, l.width)
        }
    });
}

function unHighlightFunEnrichNodes(nodes) {
    if (nodes.constructor !== Array) {
        nodes = [nodes];
    }
    nodes = nodes.map(String)
    if (current_main_plot === "score") {
        scene2.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                var n = child.userData.nodeData;
                if (n !== undefined) {
                    if (nodes.indexOf(n.id) !== -1 || nodes.indexOf(n.label) !== -1) {
                        child.material.opacity = 0.2;
                        var colorValue = parseInt(child.userData.nodeData.colorb.replace("#", "0x"), 16);
                        var col = new THREE.Color(colorValue);
                        child.material.color = col;

                        child.visible = true;
                    }
                }
            }
        });
    } else {
        Scatter.graphData().nodes.forEach(function (n) {
            if (nodes.indexOf(n.id) !== -1 || nodes.indexOf(n.label) !== -1 || nodes.indexOf(n.keggId) !== -1) {
                if (highlightType === "mixed") {
                    n.highlight = 0;
                    if (n.prevCol === undefined || n.prevCol === null) {
                        colorNodeObj(n, n.tcolor)
                        opaNodeObj(n, 0.2)
                        n.color = n.tcolor;
                    } else {
                        colorNodeObj(n, n.prevCol)
                        opaNodeObj(n, 0.2)
                        n.color = n.prevCol;
                    }
                } else {
                    unhighlightNode(n);
                }

            }
        });
    }
    setEdgeColorGrey();
}

function setEdgeColorGrey() {
    Scatter.graphData().links.forEach(function (l) {
        colorLineObj(l, edgeColor)
    });
}

function displayHighNodes() {
    var node_vec = [];
    Scatter.graphData().nodes.forEach(function (nodeUI) {
        if (nodeUI.highlight === 1) {
            node_vec.push(nodeUI);
        }
    });
    displayCurrentSelectedNodes(node_vec, "");
}

function displayReductionInfo() {
    var stats = $("#stats");
    stats.empty();

    for (var propNm in gData.misc) {
        if (propNm === "pct") {
            stats.append('<strong>' + "Contributions(X,Y,Z)" + '</strong>: ' + gData.misc[propNm][0] + '%, ' + gData.misc[propNm][1] + '%, ' + gData.misc[propNm][2] + '%');
        } else if (propNm === "pct2") {
            for (var propNm in gData.misc.pct2) {
                if (propNm === currentOmicsType) {
                    stats.append('<strong>' + "Contributions(X,Y,Z)" + '</strong>: ' + gData.misc.pct2[propNm][0] + '%, ' + gData.misc.pct2[propNm][1] + '%, ' + gData.misc.pct2[propNm][2] + '%');
                }
            }
        } else {
            stats.append('<strong>' + propNm + '</strong>: ' + gData.misc[propNm] + "<br />");

        }
    }
}

function displayCurrentSelectedNodes(nodes, title) {
    var stats = $("#stats");
    //$('#p').panel('open')
    stats.empty();
    if (title !== "") {
        stats.append('<lh><b>' + title + '</b></lh>');
    }
    for (var i = 0; i < nodes.length; i++) {
        stats.append('<li class="stats">' + nodes[i].label + '</li>');
    }
}

function displayHighlightedNode(node) {

    var stats = $("#stats");
    //$('#p').panel('open')
    stats.append('<li class="stats">' + node.label + '</li>');

}

function updateCellColor(color, id) {
    $("#" + id).css("background-color", color.replace('0x', '#'));
}

function searchNetwork(nodeID) {
    console.log("displayNodeInfo << --- 2");
    var hit = 0;
    if (current_main_plot === "score") {
        Scatter.graphData().nodes.forEach(function (n) {
            if (n.id === nodeID || n.label === nodeID) {
                hit = 1;
                n.highlight = 1;
                opaNodeObj(n, 0.8);
                shiftCameraToNode(n);
                if (highlightType === "mixed") {
                    n.prevCol = n.color;
                    if (outlineUIs[n.id] === null || outlineUIs[n.id] === undefined) {
                        outlineNode(n, highlightColor)
                    } else {
                        deleteOutline(n);
                        outlineNode(n, highlightColor)
                    }
                } else {
                    highlightNode(n);
                }
                displayNodeInfo(n);
            }
        });
    } else {
        Scatter.graphData().nodes.forEach(function (n) {
            if (n.id === nodeID || n.label === nodeID) {
                hit = 1;
                n.highlight = 1;
                opaNodeObj(n, 1)
                if (highlightType === "mixed") {
                    n.prevCol = n.color;
                    if (outlineUIs[n.id] === null || outlineUIs[n.id] === undefined) {
                        outlineNode(n, highlightColor)
                    } else {
                        deleteOutline(n);
                        outlineNode(n, highlightColor)
                    }
                } else {
                    highlightNode(n);
                }
                displayNodeInfo(n);
            } else {
                if (n.highlight !== 1) {
                    deleteOutline(n);
                    opaNodeObj(n, 0.15)
                }
                //colorNodeObj(n, "#d3d3d3")
            }
        });
    }
    //setEdgeColorGrey();
    if (!hit) {
        $.messager.alert('Error', "Node " + nodeID + " was not found in the current network!", 'error');
    }
    return;
}

function resetNodes(nodeIDs) {
    var count = 0;
    Scatter.graphData().nodes.forEach(function (n) {
        if (nodeIDs.indexOf(n.id) !== -1) {
            if (outlineUIs[n.id] !== undefined || outlineUIs[n.id] !== null) {
                deleteOutline(n);
            }

            if (highlightType === "mixed") {
                n.highlight = 0;
                if (n.prevCol === undefined || n.prevCol === null) {
                    colorNodeObj(n, n.tcolor)
                    n.color = n.tcolor;
                } else {
                    colorNodeObj(n, n.prevCol)
                    n.color = n.prevCol;
                }
            } else {
                unhighlightNode(n);
            }
        }

        if (n.highlight === 1) {
            count++;
        }
    });

    if (count === 0) {
        updateNode();
    }
}

function highlightMyNodes() {
    var ids = $('#batch').val().split('\n');
    highlightNodes(ids, highlightType);
}

function unhighlightMyNodes() {
    var ids = $('#batch').val().split('\n');
    unhighlightNodes(ids);
}

function unhighlightNodes(ids) {
    Scatter.graphData().nodes.forEach(function (n) {
        if (ids.indexOf(n.id) !== -1 || ids.indexOf(n.label) !== -1) {
            colorNodeObj(n, n.tcolor)
            if (outlineUIs[n.id] !== null || outlineUIs[n.id] !== undefined) {
                deleteOutline(n);
            }
            n.highlight = 0;
        }
    });
    //setEdgeColorGrey();
}

function highlightNodes(ids, type) {
    Scatter.graphData().nodes.forEach(function (n) {
        if (ids.indexOf(n.id) !== -1 || ids.indexOf(n.label) !== -1) {
            if (type === "node" || type === "mixed") {
                n.prevCol = n.color;
                n.highlight = 1;
                colorNodeObj(n, highlightColor)
            } else {
                highlightNode(n);
            }
        } else {
            colorNodeObj(n, "#d3d3d3")
            opaNodeObj(n, 0.2)
        }
    });
    //setEdgeColorGrey();
    displayHighNodes();
}

function displayNodeInfoMeta(node) {
    $("#dragDiv").css('display', 'none')
    var stats = $("#stats");
    var title = node.id
    var nodes = [];

    stats.empty();
    if (title !== "") {
        stats.append('<lh><b>' + title + '</b></lh>');
    }
    for (var i = 0; i < node.metanodes.length; i++) {
        stats.append('<li class="stats">' + node.metanodes[i].label + '</li>');
    }
}

function displayNodeInfoModule(inx) {
    $("#dragDiv").css('display', 'none')
    var stats = $("#stats");
    var title = "module" + inx
    var nodes = [];
    Scatter.graphData().nodes.forEach(function (n) {
        if (n.moduleNumber + "" === inx + "") {
            nodes.push(n)
        }
    });

    stats.empty();
    if (title !== "") {
        stats.append('<lh><b>' + title + '</b></lh>');
    }
    for (var i = 0; i < nodes.length; i++) {
        stats.append('<li class="stats">' + nodes[i].label + '</li>');
    }
}

function updateNode() {
    var stats = $("#stats");
    stats.empty();
    var col2 = $('#dg2').datagrid('getColumnOption', 'color');
    col2.styler = function () {
        return 'background-color:white';
    };
}

function exportResultTable(name) {
    if (name === "funtb") {
        if ($('#dge').datagrid('getRows').length === 0) {
            $.messager.alert('Error', 'No functional enrichment analysis has been performed!', 'error');
            return;
        }
    }
    if (name === "comtb") {
        setupFileDownload("module_table.csv");
    } else if (name === "funtb") {
        setupFileDownload(currentEnrichFile + ".csv");
    } else {
        doGraphExport(setupFileDownload, name);
    }
}

function setupFileDownload(result) {
    var fileLnk = $("#fileLnk");
    fileLnk.empty();
    fileLnk.append("Right click the link below, then 'Save Link As ... ' to download the file<br/><br/>");
    fileLnk.append('<strong><a href="' + usr_dir + '/' + result + '" target="_blank"><u>' + result + '</u></a></strong>');
    $.messager.progress('close');
    $("#filedialog").dialog('open');
}

function showLoad() {
    var r = $.Deferred();
    $('#loader').show();
    return r;
}

function updateTextInputSize(val) {
    document.getElementById('nsize').value = val;
}

function updateTextInputOpa(val) {
    document.getElementById('textInputOpa').value = val;
}

function updateTextInputOpaNode(val) {
    document.getElementById('textInputOpaNode').value = val;
}

function updateTextInputWidth(val) {
    document.getElementById('textInputWidth').value = val;
}

function updateTextInputBiplot(val) {
    document.getElementById('textInputBiplot').value = val;
}

function updateTextInput(val, id) {
    document.getElementById(id).value = val;
}

function closest(arr, closestTo) {
    var closest = Math.max.apply(null, arr); //Get the highest number in arr in case it match nothing.
    for (var i = 0; i < arr.length; i++) { //Loop the array
        if (arr[i] >= closestTo && arr[i] < closest)
            closest = arr[i]; //Check if it's higher than your number, but lower than your closest value
    }
    return closest; // return the value
}

function searchLoadingTable() {
    var search = $('#nodeid').val();
    if (search === "") {
        resetNetwork();
        return;
    }
    var hitrow = "NA";
    var current_row;
    for (var i = 0; i < node_rows_csize.length; i++) {
        current_row = node_rows_csize[i];
        if (current_row.ID === search | current_row.Label === search) {
            hitrow = current_row;
            var page_num = Math.ceil(i / 30);
            var row_num = i % 30;
            $('#metadg').datagrid('gotoPage', page_num);
            $('#metadg').datagrid('loadData', node_rows_csize);
            $('#metadg').datagrid('selectRow', row_num);
            break;
        }
    }
    if (hitrow === "NA") {
        $.messager.alert('', "Could not find the given node: " + search, 'error');
        return;
    }
}

function rescale2Range(inputY, yMax, yMin, xMax, xMin) {
    var percent = (inputY - yMin) / (yMax - yMin);
    var outputX = percent * (xMax - xMin) + xMin;
    return outputX;
}

function sizeNodeObj(n, size) {
    var id = n.id
    //n.size = size;
    const object = n.__threeObj
    if (object !== null && object !== undefined) {
        object.userData.scaling = [size, size, size];
    }
}

function colorNodeObj(n, color) {
    var id = n.id;
    n.color = color.replace("0x", "#");
    var colorValue = parseInt(color.replace("#", "0x"), 16);
    var col = new THREE.Color(colorValue);
    const object = n.__threeObj;
    if (object !== null && object !== undefined) {
        const clonedMaterial = object.material.clone();
        clonedMaterial.setValues({color: col});
        object.material = clonedMaterial;
    }
}

function shapeNodeObj(n, type) {

    const object = n.__threeObj
    if (object.userData.type !== type) {
        if (object !== null && object !== undefined) {
            if (object.geometry !== undefined) {
                object.geometry.dispose();
                object.geometry = undefined;
            }
            if (type === "cone") {
                var newgeom = new THREE.ConeGeometry(20, 20);
                object.geometry = newgeom;
            } else if (type === "pyramid") {
                var newgeom = new THREE.ConeGeometry(20, 20, 4);
                object.geometry = newgeom;
            } else if (type === "cube") {
                var newgeom = new THREE.CubeGeometry(25, 15, 15);
                object.geometry = newgeom;
            } else if (type === "cylinder") {
                var newgeom = new THREE.CylinderGeometry(22, 12, 18);
                object.geometry = newgeom;
            } else if (type === "ring") {
                var newgeom = new THREE.TorusGeometry(20, 3, 16, 30);
                object.geometry = newgeom;
            } else {
                var newgeom = new THREE.SphereGeometry(20, 8, 8);
                object.geometry = newgeom;
            }
        }
        object.userData.type = type
    }
}

function opaNodeObj(n, op) {
    const object = n.__threeObj;
    if (object !== null && object !== undefined) {
        const clonedMaterial = object.material.clone();
        if (op !== 1) {
            clonedMaterial.setValues({opacity: op, transparent: true});
        } else if (op === 0) {
            clonedMaterial.setValues({visible: false});
        } else {
            clonedMaterial.setValues({opacity: op, transparent: false});
        }
        object.material = clonedMaterial;
    }
}

function opaNodeObjForLabelToggle(n, op) {
    const object = n.__threeObj
    if (object !== null && object !== undefined) {
        const clonedMaterial = object.material.clone();
        if (op !== 1) {
            clonedMaterial.setValues({opacity: op, transparent: true, depthWrite: false});
        } else {
            clonedMaterial.setValues({opacity: op, transparent: false, depthWrite: true});
        }
        object.material = clonedMaterial;
    }
}

function colorLineObj(l, color) {
    var id = l.id
    l.color = color.replace("0x", "#");
    var colorValue = parseInt(color.replace("#", "0x"), 16);
    var col = new THREE.Color(colorValue);
    const object = l.__lineObj
    if (object !== undefined && object !== null) {
        const clonedMaterial = object.material.clone();
        clonedMaterial.setValues({color: col});

        object.material = clonedMaterial;
    }
}

function opaLineObj(l, opacity) {
    var id = l.id
    const object = l.__lineObj
    if (object !== undefined && object !== null) {
        const clonedMaterial = object.material.clone();
        if (opacity === 0) {
            clonedMaterial.setValues({visible: false});
            object.material = clonedMaterial;
        } else {
            clonedMaterial.setValues({opacity: opacity, visible: true});
            object.material = clonedMaterial;
        }
    }
}

function sizeLineObj(l, size) {
    var id = l.id
    const object = l.__lineObj
    if (object !== undefined && object !== null) {
        const clonedMaterial = object.material.clone();
        if (size === 0) {
            object.visible = true;
        } else {
            object.visible = false;
            var geometry = new THREE.LineGeometry();
            var pos = [l.source.fx, l.source.fy, l.source.fz, l.target.fx, l.target.fy, l.target.fz];
            geometry.setPositions(pos);
            //geometry.setColors(colors);
            var colorValue1 = parseInt(highlightColor.replace("#", "0x"), 16);
            var col1 = new THREE.Color(colorValue1);

            var matLine = new THREE.LineMaterial({
                color: col1,
                linewidth: size / 1000, // in pixels
                //vertexColors: THREE.VertexColors,
                //resolution:  // to be set by renderer, eventually
                dashed: false

            });

            var line = new THREE.Line2(geometry, matLine);
            line.computeLineDistances();
            line.scale.set(1, 1, 1);
            lineObj[l.id] = line;
            line.userData = "fatline" + l.id
            Scatter.scene().add(lineObj[l.id]);
            //Scatter.scene().remove(lineObj[l.id]);
        }
    }
}

function colorLineObj2(l, type) { // multiple color
    const object = l.__lineObj
    if (parseFloat(l.opacity) === 0) {
        //colorLineObj(l, type)
        return;
    }
    if (object.geometry !== null && object.geometry !== undefined) {
        var count = 2;
        object.geometry.addAttribute('color', new THREE.BufferAttribute(new Float32Array(count * 3), 3));

        var colorValue1 = parseInt(l.source.color.replace("#", "0x"), 16);

        var col1 = new THREE.Color(colorValue1);
        var colorValue2 = parseInt(l.target.color.replace("#", "0x"), 16);
        var col2 = new THREE.Color(colorValue2);
        var i = 0
        object.geometry.attributes.color.array[ i ] = col1["r"];
        object.geometry.attributes.color.array[ i + 1 ] = col1["g"];
        object.geometry.attributes.color.array[ i + 2 ] = col1["b"];
        object.geometry.attributes.color.array[ i + 3 ] = col2["r"];
        object.geometry.attributes.color.array[ i + 4 ] = col2["g"];
        object.geometry.attributes.color.array[ i + 5 ] = col2["b"];

        //object.material.color = col1
        object.material.vertexColors = THREE.VertexColors
        //object.geometry.dispose();
        //object.geometry = undefined;
        if (type == "cone") {
            //var newgeom =  new THREE.ConeGeometry(10, 20);
            //object.geometry = newgeom;
        }
    }

    opaLineObj(l, parseFloat(l.opacity))
}

function colorAdapt(col) {
    var colorValue = parseInt(col.replace("#", "0x"), 16);
    var color = new THREE.Color(colorValue);
    return color;
}

function iterate(obj, func) {
    for (var k in obj) {
        if (!obj.hasOwnProperty(k)) {
            continue;
        }

        func(obj[k], k);
    }
}

function strToObjectRef(obj, str) {
    // http://stackoverflow.com/a/6393943 
    if (str == null)
        return null;
    return str.split('.').reduce(function (obj, i) {
        return obj[i]
    }, obj);
}

function hexToRgb(hex) {
    // Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF") 
    var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
    hex = hex.replace(shorthandRegex, function (m, r, g, b) {
        return r + r + g + g + b + b;
    });

    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? [
        parseInt(result[1], 16),
        parseInt(result[2], 16),
        parseInt(result[3], 16)
    ] : null;
}

function determinePercentile(arr, p) {
    if (arr.length === 0)
        return 0;
    if (typeof p !== 'number')
        throw new TypeError('p must be a number');
    if (p <= 0)
        return arr[0];
    if (p >= 1)
        return arr[arr.length - 1];

    var index = arr.length * p,
            lower = Math.floor(index),
            upper = lower + 1,
            weight = index % 1;

    if (upper >= arr.length)
        return arr[lower];
    return arr[lower] * (1 - weight) + arr[upper] * weight;
}

function hex(c) {
    var s = "0123456789abcdef";
    var i = parseInt(c);
    if (i == 0 || isNaN(c))
        return "00";
    i = Math.round(Math.min(Math.max(0, i), 255));
    return s.charAt((i - i % 16) / 16) + s.charAt(i % 16);
}

/* Convert an RGB triplet to a hex string */
function convertToHex(rgb) {
    return hex(rgb[0]) + hex(rgb[1]) + hex(rgb[2]);
}

/* Remove '#' in color hex string */
function trim(s) {
    return (s.charAt(0) == '#') ? s.substring(1, 7) : s
}

/* Convert a hex string to an RGB triplet */
function convertToRGB(hex) {
    var color = [];
    color[0] = parseInt((trim(hex)).substring(0, 2), 16);
    color[1] = parseInt((trim(hex)).substring(2, 4), 16);
    color[2] = parseInt((trim(hex)).substring(4, 6), 16);
    return color;
}

function getContrastColor(hexcolor) {
    hexcolor = hexcolor.replace("#", "");
    var r = parseInt(hexcolor.substr(0, 2), 16);
    var g = parseInt(hexcolor.substr(2, 2), 16);
    var b = parseInt(hexcolor.substr(4, 2), 16);
    var yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
    return (yiq >= 128) ? '#222222' : '#ffffff';
}

function removeOptions(selectbox)
{
    if (selectbox !== undefined && selectbox !== null) {
        var i;
        for (i = selectbox.options.length - 1; i >= 0; i--)
        {
            selectbox.remove(i);
        }
    }
}

function generateSprite(color) {
    var canvas = document.createElement('canvas');
    canvas.width = 256;
    canvas.height = 256;
    var context = canvas.getContext('2d');
    var gradient = context.createRadialGradient(canvas.width / 2, canvas.height / 2, 0, canvas.width / 2, canvas.height / 2, canvas.width / 2);
    gradient.addColorStop(0, 'rgba(0,0,0,0)');
    gradient.addColorStop(0.2, 'rgba(120,120,120,0)');
    gradient.addColorStop(0.5, color.replace("0x", "#"));
    gradient.addColorStop(1, 'rgba(0,0,0,0)');
    context.fillStyle = gradient;
    context.fillRect(0, 0, canvas.width, canvas.height);
    return canvas;
}

function highlightNodeAndNeighbours(node) {
    node.highlight = 1;
    var links = Scatter.graphData().links
    var nodes = Scatter.graphData().nodes
    if (["mcia"].indexOf(gData.reductionOpt) !== -1) {
        var int_node_id = node.id.substring(0, node.id.length - 2);
        var idsArr = [node.id, int_node_id];
        var node_arr = [node]
        for (var i = 0; i < links.length; i++) {
            var link = links[i];
            if (idsArr.indexOf(link.source.id) !== -1) {
                node_arr.push(link.target);
            } else if (idsArr.indexOf(link.target.id) !== -1) {
                node_arr.push(link.source);
            }
        }
        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i].id === int_node_id) {
                node_arr.push(nodes[i])
            }
        }
    } else {
        var node_arr = [node]
        for (var i = 0; i < links.length; i++) {
            var link = links[i];
            if (link.source.id === node.id) {
                node_arr.push(link.target);
            } else if (link.target.id === node.id) {
                node_arr.push(link.source);
            }
        }
    }

    for (var i = 0; i < node_arr.length; i++) {
        var n = node_arr[i]
        if (highlightType === "mixed" || highlightType === "node") {
            outlineNode(n, highlightColor);
            n.highlight = 1
            //sizeNodeObj(node, node.size+2)
            //node.size = node.size + 2;
        } else {
            if (outlineUIs[n.id] === null || outlineUIs[n.id] === undefined) {
                outlineNode(n, highlightColor);
                n.highlight = 1
            } else {
                deleteOutline(node);
                outlineNode(n, highlightColor);
                n.highlight = 1
            }
        }
    }

    displayCurrentSelectedNodes(node_arr, "")
}

function unhighlightNode(node) {
    node.highlight = 0;
    if (highlightType === "mixed") {

    } else if (highlightType === "node") {
        const object = node.__threeObj;
        if (node.prevCol === undefined || node.prevCol === null) {
            colorNodeObj(node.id, node.tcolor)
        } else {
            colorNodeObj(node.id, node.prevCol)
        }
    }

    if (outlineUIs[node.id] !== null || outlineUIs[node.id] !== undefined) {
        deleteOutline(node);
    }
}

function updateTextInputLabel(val) {
    document.getElementById('textInputLabelThreshold').value = val;
}

function shiftCameraToNode(node) {
    const distance = 300;
    const distRatio = 1 + distance / Math.hypot(node.fx, node.fy, node.fz);

    Scatter.cameraPosition(
            {x: node.fx * distRatio, y: node.fy * distRatio, z: node.fz * distRatio}, // new position
            {x: 0, y: 0, z: 0}, // lookAt ({ x, y, z })
            3000  // ms transition duration
            );

    setTimeout(function () {
        Scatter.controls().target.set(node.fx, node.fy, node.fz)
        currentOrigin = v(node.fx, node.fy, node.fz);
    }, 3200);

}

function initData(data) {
    var node_table1 = [];
    var node_len = data.nodes.length;

    var deg_arr = [];
    for (var i = 0; i < node_len; i++) {
        var nd = data.nodes[i];
        deg_arr.push(parseInt(nd.attributes.degree));
    }

    var yMin = Math.min.apply(null, deg_arr);
    var yMax = Math.max.apply(null, deg_arr);
    for (var i = 0; i < node_len; i++) {
        var nd = data.nodes[i];
        if (parseInt(nd.attributes.degree) > 0) {

            //need to covert the attribute val to numeric, the default is string with quotes
            nd.degree = parseInt(nd.attributes.degree);
            nd.betweenness = parseFloat(nd.attributes.between);
            nd.between = parseFloat(nd.attributes.between);
            nd.expr = nd.attributes.expr;
            nd.true_color_b = nd.colorb;
            nd.true_color_w = nd.colorw;
            nd.expanded = false;
            nd.topo_b = nd.topocolb;
            nd.topo_w = nd.topocolw;
            nd.exp_b = nd.expcolb;
            nd.exp_w = nd.expcolw;
            nd.seed = nd.seed;
            nd.seedArr = nd.seedArr;
            nd.type = nd.type;

            //nd.pos = nd.pos;

            //nd.true_size = nd.size;
            nd.opacity = rescale2Range(Math.log(parseInt(nd.attributes.degree) + 1), Math.log(yMax), Math.log(yMin), 1, 0.6);
            if (isNaN(nd.opacity)) {
                nd.opacity = 0.8
            }
            if (window.devicePixelRatio > 1) {
                nd.true_size = rescale2Range(parseInt(nd.attributes.degree), yMax, yMin, 10, 2);
            } else if (window.devicePixelRatio === 1 && node_len < 100) {
                nd.true_size = rescale2Range(parseInt(nd.attributes.degree), yMax, yMin, 5, 2);
            } else {
                nd.true_size = rescale2Range(parseInt(nd.attributes.degree), yMax, yMin, 10, 1);
            }

            if (nd.type === "gene") {
                nd.typeValue = 1;
            } else if (nd.type === "interactor") {
                nd.typeValue = 0;
            } else if (nd.type === "met") {
                nd.typeValue = 2;
            } else if (nd.type === "tf") {
                nd.typeValue = 3;
            } else if (nd.type === "mir") {
                nd.typeValue = 4;
            }

            nd.color = nd.true_color_b;
           
            if (!nd.expr) {
                nd.expr = 0;
            }
            node_table1.push({
                ID: nd.id,
                Color: nd.true_color_b,
                ColorW: nd.true_color_w,
                Topo: nd.topo_b,
                TopoW: nd.topo_w,
                Exp: nd.exp_b,
                ExpW: nd.exp_w,
                Type: nd.type,
                iType: nd.itype,
                Seed: nd.seed,
                SeedArr: nd.seedArr,
                Size: nd.true_size,
                Label: nd.label,
                Degree: nd.degree,
                Betweenness: nd.between,
                //pos: nd.pos,
                Expression: nd.expr,
                User: nd.user,
                TypeValue: nd.typeValue
            });

        }
    }

    node_rows_csize = node_table1;
    //init sort the node table based on their betweenness then degree

    node_rows_csize.sort(function (a, b) {
        return b.Betweenness - a.Betweenness;
    });
    node_rows_csize.sort(function (a, b) {
        return b.Degree - a.Degree;
    });
    nodes_number = node_rows_csize.length;
    var attr_array = [];
    for (var i = 0; i < node_rows_csize.length; i++) {
        attr_array.push(node_rows_csize[i].Degree);
    }
    ;

    var i = node_rows_csize.length;
    while (i--) {
        var n = node_rows_csize[i];
        if (n.Type === "peak") {
            node_rows_csize.splice(i, 1);
        }
    }

    $('#mdg').datagrid('loadData', {
        "total": 0,
        "rows": []
    });
    initTable();
}

function percentile(arr, p) {
    if (arr.length === 0)
        return 0;
    if (typeof p !== 'number')
        throw new TypeError('p must be a number');
    if (p <= 0)
        return arr[0];
    if (p >= 1)
        return arr[arr.length - 1];

    arr.sort(function (a, b) {
        return a - b;
    });
    var index = (arr.length - 1) * p
    lower = Math.floor(index),
            upper = lower + 1,
            weight = index % 1;

    if (upper >= arr.length)
        return arr[lower];
    return arr[lower] * (1 - weight) + arr[upper] * weight;
}

function initTable() {

    $('#metadg').datagrid('clearSelections');
    var data_grid = $('#metadg');
    data_grid.datagrid('loadData', []);
    data_grid.datagrid('loadData', node_rows_csize);
    data_grid.datagrid('fitColumns', true);
}

function deleteOutline(n) {
    if (outlineUIs[n.id] !== undefined) {
        var group = n.__threeObj
        for (var i = 0; i < group.children.length; i++) {
            var child = group.children[i]
            if (child.userData.type === "halo") {
                group.remove(child);
            }
        }

        //group.remove(group.children[group.children.length - 1]);
        var mesh = outlineUIs[n.id];
        Scatter.scene().remove(mesh);
        if (mesh) {
            if (mesh.material.geometry) {
                mesh.geometry.dispose();
                mesh.geometry = undefined;
            }
            if (mesh.material.map)
                mesh.material.map.dispose();
            if (mesh.material.lightMap)
                mesh.material.lightMap.dispose();
            if (mesh.material.bumpMap)
                mesh.material.bumpMap.dispose();
            if (mesh.material.normalMap)
                mesh.material.normalMap.dispose();
            if (mesh.material.specularMap)
                mesh.material.specularMap.dispose();
            if (mesh.material.envMap)
                mesh.material.envMap.dispose();
            mesh.material.dispose();
            mesh.material = undefined;
            mesh = undefined;
            outlineUIs[n.id] = null;
            delete outlineUIs[n.id];
        }
    }
}

function addLabel(node) {
    if (node.labeled === 2) {
        return;
    }
    var myText = new SpriteText(node.label);
    myText.color = labelColOpt
    myText.visible = true;
    myText.position.y = node.size / 2 + 15;
    myText.textHeight = 12;
    myText.fontSize = 100
    myText.depthWrite = false
    myText.userData = {}
    myText.userData.id = node.label


    const elem = document.createElement('div');
    elem.textContent = node.label.replace("-", "‑");
    labelContainerElem.appendChild(elem);
    node.labeled = 1;

    node.__threeObj.add(myText)

    labelContainerElem.style.color = labelColOpt
    if (htmlTextBool) {
        elem.style.display = "block"
        myText.visible = false
    } else {
        elem.style.display = "none"
        myText.visible = true
    }
    spriteys[node.id] = elem;
    spriteysCanvas[node.id] = myText
}

function highlightSeed() {
    if (outlineBool === false) {
        if (current_main_plot === "score") {

            scene2.traverse(function (child) {
                if (child instanceof THREE.Mesh) {
                    var n = child.userData.nodeData;
                    if (n !== undefined) {
                        if (n.seedArr === "seed") {
                            child.material.opacity = 1;
                            //var colorValue = parseInt(highlightColor.replace("#", "0x"), 16);
                            //var col = new THREE.Color(colorValue);
                            //child.material.color = col;

                            child.visible = true;
                        } else {
                            child.material.opacity = 0.2;
                            var colorValue = parseInt("0xd3d3d3", 16);
                            var col = new THREE.Color(colorValue);
                            child.material.color = col;

                            child.visible = true;
                        }
                    }
                }
            });
        } else {
            var nodes = Scatter.graphData().nodes;
            nodes.forEach(function (n) {
                if (n.seedArr === "seed") {
                    outlineNode(n, highlightColor)
                }
            });

        }

        $('#loader').hide();
    }
}

function hideSeed() {
    if (outlineBool === true) {
        if (current_main_plot === "score") {

            scene2.traverse(function (child) {
                if (child instanceof THREE.Mesh) {
                    var n = child.userData.nodeData;
                    if (n !== undefined) {

                        child.material.opacity = 1;
                        var colorValue = parseInt(n.colorb.replace("#", "0x"), 16);
                        var col = new THREE.Color(colorValue);
                        child.material.color = col;

                        child.visible = true;

                    }
                }
            });
        } else {
            var nodes = Scatter.graphData().nodes;
            nodes.forEach(function (n) {
                if (n.seedArr === "seed") {
                    if (outlineUIs[n.id] !== undefined || outlineUIs[n.id] !== null) {
                        deleteOutline(n)
                    }
                }
            });

        }

        $('#loader').hide();
    }
}

function deleteSprite(node) {
    if (!htmlTextBool) {
        if (spriteysCanvas[node.id] !== undefined) {
            spriteysCanvas[node.id].material.dispose();
            spriteysCanvas[node.id].geometry.dispose();
            node.labeled = 0;

            Scatter.scene().remove(spriteysCanvas[node.id]);
            delete spriteysCanvas[node.id]
        }
    } else {

        if (spriteys[node.id] !== undefined) {
            node.labeled = 0;
            var element = spriteys[node.id]
            element.parentNode.removeChild(element);
            spriteys[node.id] = null;
            delete spriteys[node.id];
        }
    }
}

function zoomIn(distance) {
    Scatter.camera().translateZ(-distance);
}
function zoomOut(distance) {
    Scatter.camera().translateZ(distance);
}

function takeScreenshot() {
    var img = new Image();
    var scene = Scatter.scene();
    var camera = Scatter.camera()

    if (gData.navigation !== "NA") {
        takeImageBool = true;
    }
    screenshotBool = true;
    updateLabelPositions();
    Scatter.renderer().render(scene, camera);
    screenshotBool = false;
    img.src = Scatter.renderer().domElement.toDataURL();
    document.getElementById("downloadimage").src = img.src;
    document.getElementById("downloadimage").appendChild(img);

    $("#pngdialog").dialog('open');
}

var resizeId;
function onWindowResize() {
    clearTimeout(resizeId);
    resizeId = setTimeout(doneResizing, 500);
    //alert("resize");
    const container = document.getElementById("3d-graph");
    var camera = Scatter.camera();
    function doneResizing() {
        camera.aspect = container.clientWidth / container.clientHeight;
        camera.updateProjectionMatrix();
        Scatter.width(container.clientWidth);
        Scatter.height(container.clientHeight);
    }
}

function clone(obj) {
    if (null == obj || "object" != typeof obj)
        return obj;
    var copy = obj.constructor();
    for (var attr in obj) {
        if (obj.hasOwnProperty(attr))
            copy[attr] = obj[attr];
    }
    return copy;
}

function addMetaSphere(shownodes, inx) {
    if (boundingClusters[inx] !== undefined) {
        deleteMetaSphere(inx, Scatter.scene())
    }
    var vertices = [];
    shownodes.forEach(function (n) {
        var pos = new THREE.Vector3()
        pos.x = n.x
        pos.y = n.y
        pos.z = n.z
        vertices.push(pos)
    })

    var color = highlightColor.replace("0x", "#");
    var colorValue = parseInt(color.replace("#", "0x"), 16);
    var col = new THREE.Color(colorValue);

    var sphere = new THREE.Sphere().setFromPoints(vertices);
    var sphereGeometry = new THREE.SphereGeometry(sphere.radius + 10, 64, 64);
    var sphereMaterial = new THREE.MeshPhongMaterial({
        opacity: 0.3,
        color: col,
        transparent: true,
        depthWrite: false
    });

    var sphereMes = new THREE.Mesh(sphereGeometry, sphereMaterial);
    sphereMes.position.set(sphere.center.x, sphere.center.y, sphere.center.z)
    boundingClusters[inx] = sphereMes
    activeModules[inx] = inx;
    Scatter.scene().add(sphereMes);
}

function deleteShade(t) {
    var group = t.__threeObj
    if (group !== undefined) {
        for (var i = group.children.length - 1; i >= 0; i--) {
            var myMesh = group.children[i];
            myMesh.geometry.dispose();
            myMesh.material.dispose();
            myMesh = null;
            group.remove(group.children[i]);
        }

        group.geometry.dispose();
        group.material.dispose();
        group = null
        delete t.__threeObj
    }
    var nobj = t.__threeObj;
    if (nobj !== undefined) {
        for (var i = nobj.children.length - 1; i >= 0; i--) {
            var myMesh = nobj.children[i];
            myMesh.geometry.dispose();
            myMesh.material.dispose();
            myMesh = null;
            t.__threeObj.remove(nobj.children[i]);
        }

        t.__threeObj.geometry.dispose();
        t.__threeObj.material.dispose();
        t.__threeObj = null
        delete t.__threeObj
        //Scatter.scene().remove(nobj)
    }
}

function setShade() {
    setTimeout(function () {
        hoveredNode = null;

        Scatter.graphData().nodes.forEach(function (n) {
            deleteSprite(n)
        })
        spriteys = null;
        spriteys = {};

        var nodes_num = Scatter.graphData().nodes.length
        for (var i = 0; i < nodes_num; i++) {
            var node = Scatter.graphData().nodes[i];
            var obj = node.__threeObj;
            var material;
            var spritex;
            // add text sprite as child
            if (labelMode === "global") {
                if (node.attributes !== undefined) {
                    if (nodes_num > 50) {
                        //addLabel(node)
                    } else {
                        //addLabel(node)
                    }
                }
            }
            if (node.type === "metabolite") {
                scatterType = "multi";
                node.tcolor = highlightColor;
                node.color = highlightColor;
                colorNodeObj(node, highlightColor);
            } else if (node.type === "microbe") {
                node.tcolor = "#ff0000";
                node.color = "#ff0000";
                colorNodeObj(node, "#ff0000");
            }
        }
        var deg_arr = {}
        var deg_threshold = {}

        if (labelMode === "module") {
            for (var i = 0; i < sub_modules.length; i++) {
                deg_arr[i] = []
                Scatter.graphData().nodes.forEach(function (n) {
                    if (n.module === i) {
                        deg_arr[i].push(n[labelProperty]);
                    } else {

                    }
                })
                //if(deg_top25 === 0){
                var deg_top25 = Math.max(...deg_arr[i]);
                //}
                deg_threshold[i] = deg_top25;
            }

            Scatter.graphData().nodes.forEach(function (n) {
                if (parseInt(n[labelProperty]) >= parseInt(deg_threshold[n.module])) {
                    addLabel(n)
                } else if (n.attributes !== undefined && n.module === undefined) {

                    if (n[labelProperty] > labeling_threshold && spriteys[n.id] === undefined) {
                        idsArr.push(n.id);
                        addLabel(n)
                    }
                }
            });


        }
        var links_num = Scatter.graphData().links.length

        for (var i = 0; i < links_num; i++) {
            var l = Scatter.graphData().links[i]
            if (l.label !== undefined) {
                // addLabelLink(l);
            }
            //var between = (l.source.between + l.target.between + 1)/2
            /*if (links_num < 500) {
             l.opacity = 1;
             } else if (links_num > 500 && links_num < 1500) {
             l.opacity = 0.8;
             } else {
             l.opacity = 0.5;
             }*/
            if (!spherical && !bundled) {
                opaLineObj(l, l.opacity);
            }
        }

    }, 1000);
}

function LightenDarkenColor(col, amt) {

    var usePound = false;

    if (col[0] === "#") {
        col = col.slice(1);
        usePound = true;
    }

    var num = parseInt(col, 16);

    var r = (num >> 16) + amt;

    if (r > 255)
        r = 255;
    else if (r < 0)
        r = 0;

    var b = ((num >> 8) & 0x00FF) + amt;

    if (b > 255)
        b = 255;
    else if (b < 0)
        b = 0;

    var g = (num & 0x0000FF) + amt;

    if (g > 255)
        g = 255;
    else if (g < 0)
        g = 0;

    return (usePound ? "#" : "") + (g | (b << 8) | (r << 16)).toString(16);

}

function setHighOpt() {
    var val = $('#highOpt').val();
    if (val === "mixed") {
        highlightType = "mixed";
    }
    if (val === "color") {
        highlightType = "node";
    } else if (val === "halo") {
        highlightType = "halo";
    } else if (val === "unh") {
        Scatter.graphData().nodes.forEach(function (n) {
            if (n.prevCol === undefined || n.prevCol === null) {
                colorNodeObj(n, n.tcolor)
            } else {
                colorNodeObj(n, n.prevCol)
            }
            //n.color = n.tcolor;
            if (outlineUIs[n.id] !== null || outlineUIs[n.id] !== undefined) {
                deleteOutline(n);
            }
            n.highlight = 0;
        });
    } else {
        return;
    }
}

function downloadJson(obj) {

    var filename = "MetaboAnalyst.json";
    var contentType = "application/json;charset=utf-8;";
    var fileLnk = $("#fileLnk");
    var file = new Blob([obj], {type: contentType});
    fileLnk.empty();
    fileLnk.append("Right click the link below, then 'Save Link As ... ' to download the file<br/><br/>");

    let dataStr = JSON.stringify(obj);
    let dataUri = 'data:application/json;charset=utf-8,' + encodeURIComponent(dataStr);
    let exportFileDefaultName = 'MetaboAnalyst.json';

    let linkElement = document.createElement('a');
    linkElement.setAttribute('href', dataUri);
    linkElement.setAttribute('download', exportFileDefaultName);
    linkElement.innerHTML = '<u>' + exportFileDefaultName + '</u>';

    fileLnk.append(linkElement);
    //$.messager.progress('close');
    $("#filedialog").dialog('open');
}

function changeBackground(color1, color2) {
    
    var col = color1.replace("0x", "#");
    bgColor = color1;
    if (!mainCheckBool) {        
        bgColor2 = color1;
        var colorValue = parseInt(color1.replace("#", "0x"), 16);
        var col = new THREE.Color(colorValue);
        //scene2.background = col;
        var type = $("#axisOpt").val();
        axisColorU = getContrastColor(color1.replace("#", ""));                    
        generateAxes(scene2);
        generateAxisLabel(type, scene2);

    } else {        
        var type = $("#axisOpt").val();
        bgColor = color1;
        //Scatter.backgroundColor(color1);

        var labelCol = getContrastColor(color1.replace("#", ""));
        if (labelCol !== labelColOpt) {
            labelColOpt = labelCol;
            labelContainerElem.style.color = labelColOpt;
            labelColSpectrum.setSpectrum(color1, labelCol);

            for (var propertyNm in spriteysCanvas) {               
                if (spriteysCanvas[propertyNm] !== undefined)
                    spriteysCanvas[propertyNm].color = labelColOpt;
            }
        }
        axisColorU = getContrastColor(color1.replace("#", ""));
        generateAxes(Scatter.scene());
        generateAxisLabel(type, Scatter.scene());
    }

    //document.getElementById('testBg').style.backgroundColor = color;
    //document.getElementById('testBg2').style.backgroundColor = color.toHexString();
    setGradientColor(color1, color2);
    gradCol1 = color1;
    gradCol2 = color2;
}

function updateMetaSphere() {
    var propertyNms = Object.keys(boundingClusters)
    for (var i = 0; i < propertyNms.length; i++) {
        var shownodes = [];

        Scatter.graphData().nodes.forEach(function (n) {
            if (n.module + "" === propertyNms[i]) {
                shownodes.push(n)
            }
        })
        var vertices = [];
        shownodes.forEach(function (n) {
            var pos = new THREE.Vector3()
            pos.x = n.x
            pos.y = n.y
            pos.z = n.z
            vertices.push(pos)
        })
        var sphere = new THREE.Sphere().setFromPoints(vertices);
        var sphe = boundingClusters[propertyNms[i]];
        sphe.position.set(sphere.center.x, sphere.center.y, sphere.center.z)
        //var sphe1 = boundingClustersMesh[propertyNms[i]];
        //sphe1.position.set(sphere.center.x, sphere.center.y, sphere.center.z)
    }
}

function addMetaSphereExperimental(shownodes, inx) {
    if (boundingClusters[inx] !== undefined) {
        deleteMetaSphere(inx, Scatter.scene())
    }
    var shownodesids = []
    shownodes.forEach(function (n) {
        shownodesids.push(n.id);
    })


    Scatter.graphData().links.forEach(function (l) {

        if (shownodesids.indexOf(l.source.id) !== -1 || shownodesids.indexOf(l.target.id) !== -1) {
            l.module = 1;
        }

        if (l.source.module !== l.target.module) {
            l.module = 0;
        }
    })

    var inx2 = inx % 20
    Scatter.graphData().nodes.forEach(function (n) {
        if (shownodesids.indexOf(n.id) !== -1) {
            sizeNodeObj(n, n.tsize / 1.5);
            n.moduleNumber = inx
            //colorNodeObj(n, highlightColor)
        }
    })

}

function startAnimation() {
    radius = Scatter.camera().position.distanceTo(Scatter.scene().position)
    animationVal = 0;
    takeVideo = true;
    $('#animationdlg').dialog('close');
    $('#loader').show();
    //startRecording();
}

function computeSceneRadius() {
    var nodes = Scatter.graphData().nodes;
    var scene = Scatter.scene()
    var radius = 0;
    nodes.forEach(function (n) {
        var distance = distanceVector(n, scene.position)
        radius = Math.max(distance, radius)
    })
    return(radius)
}

function initNodeSize() {
    document.getElementById('nsize').value = 2;
    //document.getElementById('minclust').value = 1;
    document.getElementById('nodeSliderSize').value = 2;
}

function zoomCheck() { //zoom results in label showin up  
    if (hideLabel || labelNodeMode) {
        return;
    }

    var scaleFactor = 0.1; // a constant  

    var defaultDepth = 500; // a constant  

    var camera = Scatter.camera()
    camera.updateMatrix();
    camera.updateMatrixWorld();
    var frustum = new THREE.Frustum();
    frustum.setFromMatrix(new THREE.Matrix4().multiplyMatrices(camera.projectionMatrix, camera.matrixWorldInverse));
    var vFOV = camera.fov * Math.PI / 180;
// Your 3d point to check  

    //var pos = new THREE.Vector3(x, y, z);  

    if (Scatter.graphData().nodes.length < 500) {
        Scatter.graphData().nodes.forEach(function (n) {

            var coord = {x: n.fx, y: n.fy, z: n.fz};
            var coordv = new THREE.Vector3(n.fx, n.fy, n.fz)

            if (frustum.containsPoint(coord)) {

                var dist = distanceVector(coord, camera.position); // convert vertical fov to radians  

                var height = 2 * Math.tan(vFOV / 2) * dist;
                var aspect = window.width / window.height;
                var width = height * aspect;
                var fraction = n.size * 15 / height;
                var elem = spriteys[n.id]
                if (!fixedLabels) {

                    if (fraction > 0.40 && spriteysCanvas[n.id] === undefined && n.labeled !== 1) {
                        if (n.labeled === 2) {
                            spriteysCanvas[n.id].material.visible = true
                        } else {
                            if (!htmlTextBool) {
                                addLabel(n);
                            }
                            n.labeled = 2;
                        }
                    } else if (fraction > 0.4 && spriteysCanvas[n.id] !== undefined && n.labeled !== 1) {
                        spriteysCanvas[n.id].material.visible = true

                        n.labeled = 2;
                    } else if (fraction < 0.38 && spriteysCanvas[n.id] !== undefined && n.labeled !== 1) {
                        spriteysCanvas[n.id].material.visible = false

                        n.labeled = -1;
                    }
                }


                if (spherical && coordv.sub(campos).length() > L && (idsArr.indexOf(n.id) !== -1)) {
                    spriteys[n.id].style.display = "none";
                    spriteysCanvas[n.id].material.visible = true
                } else if (idsArr.indexOf(n.id) !== -1 && spherical) {
                    spriteys[n.id].style.display = "block";
                    spriteysCanvas[n.id].material.visible = false
                }
            }
        });
    } else {
        return;
        var nodeDists = {}
        Scatter.graphData().nodes.forEach(function (n) {
            var coord = {x: n.fx, y: n.fy, z: n.fz};
            var coordv = new THREE.Vector3(n.fx, n.fy, n.fz)

            if (frustum.containsPoint(coord)) {
                var dist = distanceVector(coord, camera.position);
                nodeDists[n.id] = dist
            }
        });

        var dists = Object.keys(nodeDists).map(function (key) {
            return {key: key, value: this[key]};
        }, nodeDists);
        dists.sort(function (p1, p2) {
            return p1.value - p2.value;
        });

        var topTenObj = dists.slice(0, 10).reduce(function (obj, prop) {
            obj[prop.key] = prop.value;
            return obj;
        }, {});
        var ids_to_label = Object.keys(topTenObj)
        Scatter.graphData().nodes.forEach(function (n) {
            var coord = {x: n.fx, y: n.fy, z: n.fz};
            var coordv = new THREE.Vector3(n.fx, n.fy, n.fz)

            if (frustum.containsPoint(coord)) {
                var dist = distanceVector(coord, camera.position); // convert vertical fov to radians  

                var height = 2 * Math.tan(vFOV / 2) * dist;
                var aspect = window.width / window.height;
                var width = height * aspect;
                var fraction = n.size * 15 / height;
                if (!fixedLabels) {

                    if (fraction > 0.40 && spriteysCanvas[n.id] === undefined && n.labeled !== 1) {
                        if (n.labeled === 2 && ids_to_label.indexOf(n.id) !== -1) {
                            spriteysCanvas[n.id].material.visible = true
                        } else {
                            if (!htmlTextBool && ids_to_label.indexOf(n.id) !== -1) {
                                addLabel(n);
                            }
                            n.labeled = 2;
                        }
                    } else if (fraction > 0.4 && spriteysCanvas[n.id] !== undefined && n.labeled !== 1) {
                        if (ids_to_label.indexOf(n.id) !== -1) {
                            spriteysCanvas[n.id].material.visible = true
                            n.labeled = 2;
                        }
                    } else if (fraction < 0.38 && spriteysCanvas[n.id] !== undefined && n.labeled !== 1) {
                        spriteysCanvas[n.id].material.visible = false
                        n.labeled = -1;
                    }
                }

            }
        });
    }

    setTimeout(function () {
        updateLabel();
    }, 1)
}

function updateGeometries() {
    Scatter.nodeRelSize(4); // trigger update of 3d objects in scene
}

function strength(link) {
    return 1 / Math.min(count(link.source), count(link.target));
}

function count(node) {
    var links = Scatter.graphData().links
    var c = 0
    links.forEach(function (l) {
        if (l.source.id === node.id || l.target.id === node.id) {
            c++;
        }
    })
    return(c)
}



function openModuleColorPickerAll(row, index, typeString) {
    document.getElementById('selectId').innerHTML = current_selected_row.name;
    current_module = typeString + "" + index
    var inx2 = selectedMeta.indexOf(row.name);
    if (inx2 > -1) {
        selectedMeta.splice(inx2, 1)
    }
    selectedMeta.push(row.name)

    if (typeString.includes("cluster")) {
        current_point_color = highlightColor
    } else {

        current_point_color = $("#" + current_module).css("backgroundColor");
        var a = current_point_color.split("(")[1].split(")")[0];
        a = a.split(",");
        current_point_color = "#" + convertToHex(a)
    }

    if (current_point_color !== "#ffffff") {
        $("#customModule").spectrum("set", current_point_color);
        current_encasing_color = current_point_color;
        $("#customEncasing").spectrum("set", current_point_color);
        // $('#modulecolordlg').dialog('open');
    }
}

function collisionRadius(node) {
    return(node.tsize)
}

function getAllIndexes(arr, val) {
    var indexes = [], i = -1;
    while ((i = arr.indexOf(val, i + 1)) != -1) {
        indexes.push(i);
    }
    return indexes;
}

function v(x, y, z) {
    return new THREE.Vector3(x, y, z);
}
var ellipsoidgeometry, ellipsoidmesh;
function initCube(scene) {

    generateAxes(scene);
    initPlane(scene);

    if (gData.links.length === 0) {
        Scatter.graphData().nodes.forEach(function (n) {

        });
    } else {
        Scatter.graphData().nodes.forEach(function (n) {
            n.tcolor = n.color
            colorNodeObj(n, n.color)
        });
    }

    var light = new THREE.SpotLight(0xd3d3d3);
    light.intensity = 0.5;
    light.position.set(500, 2500, 0);
    light.lookAt(0, 0, 0);
    light.castShadow = true;
    light.userData.shadow = 1;

    light.shadow.mapSize.width = 2024;
    light.shadow.mapSize.height = 2024;

    light.shadow.camera.near = 10;
    light.shadow.camera.far = 5000;
    light.shadow.camera.fov = 30;// default

    Scatter.scene().traverse(function (child) {
        if (child instanceof THREE.DirectionalLight) {
            child.visible = false;
        }
        if (child instanceof THREE.AmbientLight) {
            child.intensity = 0.5;
        }
    });
    scene.add(light);

    //var helper = new THREE.CameraHelper(light.shadow.camera)
    //Scatter.scene().add(helper);
}

function createText2D(text, sceneId) {
    var myText = new SpriteText(text);
    if (myTexts[text + "" + sceneId] !== undefined) {
        myTexts[text + "" + sceneId].visible = false;
    }
    if (sceneId === scene2.uuid) {
        labelColor2 = getContrastColor(bgColor2.replace("#", ""));
        myText.color = labelColor2;
    } else {
        labelColor = getContrastColor(bgColor.replace("#", ""));
        myText.color = labelColor;
    }

    myText.visible = true;
    //myText.position.y = 15;
    myText.textHeight = 12;
    myText.fontSize = 100;
    myText.depthWrite = false;
    myText.userData = {};
    myText.userData.id = text;
    myTexts[text + "" + sceneId] = myText;
    return(myText);
}

function ellipsoid() {
    var latitudeBands = 30, longitudeBands = 20, a = 6, b = 7, c = 20, size = 40;
    for (var latNumber = 0; latNumber <= latitudeBands; latNumber++)
    {
        var theta = (latNumber * Math.PI * 2 / latitudeBands);
        var sinTheta = Math.sin(theta);
        var cosTheta = Math.cos(theta);

        for (var longNumber = 0; longNumber <= longitudeBands; longNumber++)
        {
            var phi = (longNumber * 2 * Math.PI / longitudeBands);
            var sinPhi = Math.sin(phi);
            var cosPhi = Math.cos(phi);


            var x = a * cosPhi * cosTheta;
            var y = b * cosTheta * sinPhi;
            var z = c * sinTheta;
            ellipsoidgeometry.vertices.push(new THREE.Vector3(x * size, y * size, z * size));

        }


    }
    for (var latNumber = 0; latNumber < latitudeBands; latNumber++) {
        for (var longNumber = 0; longNumber < longitudeBands; longNumber++) {
            var first = (latNumber * (longitudeBands + 1)) + longNumber;
            var second = first + longitudeBands + 1;
            ellipsoidgeometry.faces.push(new THREE.Face3(first, second, first + 1));
            ellipsoidgeometry.faces.push(new THREE.Face3(second, second + 1, first + 1));

        }
    }
}

function computeEllipsoid(scene, ids, color, type, group = "NA", alphaVal) {
    if (group !== "NA") {
        current_module = group;
    }
    if (current_module !== "NA" && current_module !== null) {
        deleteMetaSphere(current_module, scene);
        contours_info_obj[current_module] = {ids: ids, color: color, type: type}
    }
    shape_type = type

    var col;
    var mesh;

    for (var i = 0; i < gData.ellipse.length; i++) {
        var ob = gData.ellipse[i].vb
        var points = []
        var xArr = [];
        var yArr = [];
        var zArr = [];
        for (var j = 0; j < ob[0].length; j++) {
            points.push(v(ob[0][j] * 1000, ob[1][j] * 1000, ob[2][j] * 1000));
        }

        var geom = new THREE.ConvexGeometry(points)
        var opp = 0.7
        opp = opp + 0.3
        var tr = true;
        if (opp > 0.7) {
            opp = 0.7
            col = 0x431c53
        } else {
            if (opp > 0.6) {
                opp = 1
                col = 0xDA70D6
            } else {
                opp = 0.25
                col = 0xDA70D6
            }
            tr = true
        }
        var material = new THREE.MeshPhongMaterial({color: color, transparent: true, alphaTest: 0.1, opacity: alphaVal, side: THREE.BackSide});
        mesh = new THREE.Mesh(geom, material)
        var nds;
        if (current_main_plot === "score") {
            var nds = Scatter.graphData().nodes
        } else {
            var nds = gData.navigation
        }
        nds.forEach(function (n) {
            if (ids.indexOf(n.id) !== -1) {
                xArr.push(n.fx)
                yArr.push(n.fy)
                zArr.push(n.fz)
            }
        })
        var meanX = meanF(xArr)
        var meanY = meanF(yArr)
        var meanZ = meanF(zArr)
        var meanXYZ = v(meanX, meanY, meanZ)

        //geom.mergeVertices();
        //geom.computeVertexNormals()
        var clusterGroup = new THREE.Object3D();
        mesh.userData.ids = ids;
        mesh.userData.centroid = meanXYZ
        mesh.userData.shadow = 1
        mesh.castShadow = false
        mesh.receiveShadow = false
        //mesh.scale.set(1000, 1000, 1000)
        mesh.focused = false;
        var colorValue = parseInt(color.replace("#", "0x"), 16);
        var colored = new THREE.Color(colorValue);
        mesh.userData.tcolor = colored
        mesh.userData.tcolorhex = color
        mesh.userData.opacity = opp
        clusterGroup.userData = ids;
        clusterGroup.centroid = meanXYZ
        //mesh.scale.set(1000, 1000, 1000)
        if (type === "ellipse") {
            mesh.position.set(meanX, meanY, meanZ)
        } else {
            mesh.position.set(0, 0, 0)
        }
        clusterGroup.focused = false;
        clusterGroup.add(mesh)
        scene.add(clusterGroup);
        boundingClusters[current_module] = mesh;
        //boundingClustersGroup[current_module] = clusterGroup;
    }
    var sceneCenter = v(0, 0, 0)
    for (var propertyNm in boundingClustersGroup) {
        var obj = boundingClustersGroup[propertyNm];
        obj.clusterNum = propertyNm
        var clusterCenter = obj.centroid
        var dir = new THREE.Vector3(); // create once an reuse it
        dir.subVectors(clusterCenter, sceneCenter).normalize();

        // scalar to simulate speed
        var speed = 0.05;
        var vector = dir.multiplyScalar(1000, 1000, 1000);
        obj.explosionDirection = vector;
        obj.targetPosition = new THREE.Vector3()
        obj.targetPosition.x += obj.explosionDirection.x;
        obj.targetPosition.y += obj.explosionDirection.y;
        obj.targetPosition.z += obj.explosionDirection.z;
        obj.targetPositionCopy = JSON.parse(JSON.stringify(obj.targetPosition));

        Scatter.graphData().nodes.forEach(function (n) {
            if (obj.userData.indexOf(n.id) !== -1) {
                // n.explosionDirection = obj.explosionDirection
                //obj.add(n.__threeObj)
            }
        })
}
}

function meanF(arr) {
    //Find the sum
    var sum = 0;
    for (var i in arr) {
        sum += arr[i];
    }
    //Get the length of the array
    var numbersCnt = arr.length;
    //Return the average / mean.

    return (sum / numbersCnt);
}

function explodingTween(mesh, exploded) {
    var quart = new THREE.Quaternion();
    var tweenA = new TWEEN.Tween(mesh.position).to({x: mesh.targetPosition.x,
        y: mesh.targetPosition.y,
        z: mesh.targetPosition.z},
            2 * mesh.position.distanceTo(mesh.targetPosition))
            .easing(TWEEN.Easing.Linear.None).onUpdate(function (timestamp) {

    }).start();

    tweenA.onComplete(function () {


        setTimeout(function () {

        }, 1000)

    });

    animate1()
    function animate1() {
        var start = Date.now() // note this
        function loop() {
            if (Date.now() - start < 15000) { //note this also
                requestAnimationFrame(loop);
                TWEEN.update();
            }
        }
        loop();
    }

}

function computeContoursNew(scene, ids) {
    shape_type = "contour"

    var clusterMax = 1;
    for (var i = 0; i < gData.nodes.length; i++) {
        clusterMax = Math.max(clusterMax, gData.nodes[i].cluster);
    }

    var colObj = {};
    var newObj = {}
    var opaObj = {};

    for (var i = 0; i < clusterMax; i++) {
        var nodesArr = [];
        for (var j = 0; j < gData.nodes.length; j++) {
            var n = gData.nodes[j];
            if (n.cluster === i + 1) {
                nodesArr.push(v(n.fx, n.fy, n.fz))
            }
        }
        newObj[i] = nodesArr
        colObj[i] = Set2[i];
        opaObj[i] = 0.4

    }

    var xArr = [];
    var yArr = [];
    var zArr = [];
    for (var propNm in newObj) {
        if (Object.prototype.hasOwnProperty.call(newObj, propNm)) {
            var ob = newObj[propNm];
            var opa = opaObj[propNm]
            var color1 = colObj[propNm]
            var colorValue = parseInt(color1.replace("#", "0x"), 16);
            var color = new THREE.Color(colorValue);

            var meanX = meanF(xArr)
            var meanY = meanF(yArr)
            var meanZ = meanF(zArr)
            var mean = v(meanX, meanY, meanZ)

            var tr = true
            var geomm = new THREE.ConvexBufferGeometry(ob);
            var material = new THREE.MeshPhongMaterial({color: color, transparent: tr, alphaTest: 0.1, opacity: opa, depthTest: true, depthWrite: true});
            var mesh = new THREE.Mesh(geomm, material)

            mesh.position.set(0, 0, 0)
            var nds = Scatter.graphData().nodes
            var nodes_vec = [];
            nds.forEach(function (n) {
                if (n.cluster === parseInt(propNm) + 1) {
                    nodes_vec.push(n.id)
                }
            })
            Scatter.scene().add(mesh);
            //}
            var clusterGroup = new THREE.Object3D();
            mesh.userData.ids = nodes_vec;
            mesh.userData.centroid = mean
            mesh.userData.shadow = 1
            mesh.castShadow = false
            mesh.receiveShadow = false
            //mesh.scale.set(1000, 1000, 1000)
            mesh.focused = false;
            var colorValue = parseInt(color1.replace("#", "0x"), 16);
            var colored = new THREE.Color(colorValue);
            mesh.userData.tcolor = colored
            mesh.userData.tcolorhex = color1
            mesh.userData.opacity = opa + 0.3
            clusterGroup.userData = nodes_vec;
            clusterGroup.centroid = mean
            //mesh.scale.set(1000, 1000, 1000)
            clusterGroup.focused = false;
            mesh.position.set(0, 0, 0)
            clusterGroup.add(mesh)
            scene.add(clusterGroup);
            boundingClusters[parseInt(propNm) + 1] = mesh;
            boundingClustersGroup[parseInt(propNm) + 1] = clusterGroup;
        }
    }
    var sceneCenter = v(0, 0, 0)
    for (var propertyNm in boundingClustersGroup) {
        var obj = boundingClustersGroup[propertyNm];
        obj.clusterNum = propertyNm
        var clusterCenter = obj.centroid
        var dir = new THREE.Vector3(); // create once an reuse it
        dir.subVectors(clusterCenter, sceneCenter).normalize();

        // scalar to simulate speed
        var speed = 0.05;
        var vector = dir.multiplyScalar(1000, 1000, 1000);
        obj.explosionDirection = vector;
        obj.targetPosition = new THREE.Vector3()
        obj.targetPosition.x += obj.explosionDirection.x;
        obj.targetPosition.y += obj.explosionDirection.y;
        obj.targetPosition.z += obj.explosionDirection.z;
        obj.targetPositionCopy = JSON.parse(JSON.stringify(obj.targetPosition));
        Scatter.graphData().nodes.forEach(function (n) {
            if (obj.userData.indexOf(n.id) !== -1) {
                //n.explosionDirection = obj.explosionDirection
                //obj.add(n.__threeObj)
            }
        })
    }
    setTimeout(function () {
        //populateCluster();
    }, 1000)


}

function computeContours(scene, ids, color, group, alphaVal) {

    if (group !== "NA") {
        current_module = group;
    }
    if (current_module !== "NA" && current_module !== null) {
        deleteMetaSphere(current_module, scene);
        contours_info_obj[current_module] = {ids: ids, color: color, type: "contour"}
    }



    if (gData.objects === "NA") {
        return;
    }
    var colObj = {};
    var newObj = {}
    var opaObj = {};
    for (var propNm in gData.objects) {
        var ob = gData.objects[propNm];
        if (ob.type === "triangles") {
            var col = "rgb(" + Math.round(ob.colors[0]["r"] * 255) + ", " + Math.round(ob.colors[0]["g"] * 255) + ", " + Math.round(ob.colors[0]["b"] * 255) + ")";
            var a = col.split("(")[1].split(")")[0];
            a = a.split(",");
            var colHex = "#" + convertToHex(a)
            var clusterInx = Set2.indexOf(colHex);
            if (newObj[clusterInx] === undefined) {
                newObj[clusterInx] = [];
            }
            newObj[clusterInx].push(...ob.vertices);
            colObj[clusterInx] = colHex;
        }
    }

    var xArr = [];
    var yArr = [];
    var zArr = [];
    for (var propNm in newObj) {
        if (Object.prototype.hasOwnProperty.call(newObj, propNm)) {
            var ob = newObj[propNm];
            var geomm = new THREE.Geometry()
            //var vertices = [];
            //var faces = [];

            for (var i = 0; i < ob.length; i++) {
                var vv = ob[i]
                xArr.push(vv.x * 1000)
                yArr.push(vv.y * 1000)
                zArr.push(vv.z * 1000)
                geomm.vertices.push(v(vv.x * 1000, vv.y * 1000, vv.z * 1000));
            }


            var meanX = meanF(xArr)
            var meanY = meanF(yArr)
            var meanZ = meanF(zArr)
            var mean = v(meanX, meanY, meanZ)

            for (var i = 0; i < ob.length; i = i + 3) {
                var face = new THREE.Face3(i, i + 1, i + 2);

                geomm.faces.push(face);
            }

            var tr = true


            //if(opp<0.69){

            var material = new THREE.MeshPhongMaterial({color: color, transparent: tr, alphaTest: 0.1, opacity: alphaVal, depthTest: false, depthWrite: false});
            var mesh = new THREE.Mesh(geomm, material)
            geomm.mergeVertices();
            geomm.computeVertexNormals()
            //mesh.scale.set(1000, 1000, 1000)
            mesh.position.set(0, 0, 0)
            var nds;
            if (scene === Scatter.scene()) {
                nds = Scatter.graphData().nodes
            } else {
                nds = nav_nodes_arr
            }

            var nodes_vec = [];
            nds.forEach(function (n) {
                if (n.cluster === parseInt(propNm) + 1) {
                    nodes_vec.push(n.id)
                }
            })
            scene.add(mesh);
            //}
            var clusterGroup = new THREE.Object3D();
            mesh.userData.ids = ids;
            mesh.userData.centroid = mean
            mesh.userData.shadow = 1
            mesh.castShadow = false
            mesh.receiveShadow = false
            //mesh.scale.set(1000, 1000, 1000)
            mesh.focused = false;
            var colorValue = parseInt(color.replace("#", "0x"), 16);
            var colored = new THREE.Color(colorValue);
            mesh.userData.tcolor = colored
            mesh.userData.tcolorhex = color
            mesh.userData.opacity = 0.3
            clusterGroup.userData = ids;
            clusterGroup.centroid = mean
            //mesh.scale.set(1000, 1000, 1000)
            clusterGroup.focused = false;
            mesh.position.set(0, 0, 0);
            clusterGroup.add(mesh);
            scene.add(clusterGroup);
            boundingClusters[current_module] = mesh;
            boundingClustersGroup[current_module] = clusterGroup;
        }
    }

    Scatter.graphData().nodes.forEach(function (n) {
        xArr.push(n.fx)
        yArr.push(n.fy)
        zArr.push(n.fz)
    })
    var meanX = meanF(xArr)
    var meanY = meanF(yArr)
    var meanZ = meanF(zArr)
    var sceneCenter = v(meanX, meanY, meanZ)

    for (var propertyNm in boundingClustersGroup) {
        var obj = boundingClustersGroup[propertyNm];
        obj.clusterNum = propertyNm
        var clusterCenter = obj.centroid
        var dir = new THREE.Vector3(); // create once an reuse it
        dir.subVectors(clusterCenter, sceneCenter).normalize();

        // scalar to simulate speed
        var speed = 0.05;
        var vector = dir.multiplyScalar(1000, 1000, 1000);
        obj.explosionDirection = vector;
        obj.targetPosition = new THREE.Vector3()
        obj.targetPosition.x += obj.explosionDirection.x;
        obj.targetPosition.y += obj.explosionDirection.y;
        obj.targetPosition.z += obj.explosionDirection.z;
        obj.targetPositionCopy = JSON.parse(JSON.stringify(obj.targetPosition));
        Scatter.graphData().nodes.forEach(function (n) {
            if (obj.userData.indexOf(n.id) !== -1) {
                //n.explosionDirection = obj.explosionDirection
                //obj.add(n.__threeObj)
            }
        })
    }
    if (scene === Scatter.scene()) {
        //populateCluster();
    }
}
var Set2 = ["#66c2a5", "#fc8d62", "#8da0cb", "#e78ac3", "#a6d854", "#ffd92f", "#e5c494", "#b3b3b3"];

function populateCluster() {
    var nodes = Scatter.graphData().nodes
    var clusters = {};
    /*for (var i = 0; i < nodes.length; i++) {
     var n = nodes[i]
     if (clusters[n.cluster] === undefined) {
     clusters[n.cluster] = [n.id]
     } else {
     clusters[n.cluster].push(n.id);
     }
     }*/

    for (var i = 0; i < nodes.length; i++) {
        var n = nodes[i]
        if (clusters[n.metatype] === undefined) {
            clusters[n.metatype] = {}
            clusters[n.metatype][n.cluster] = [n.id];
        } else {
            if (clusters[n.metatype][n.cluster] === undefined) {
                clusters[n.metatype][n.cluster] = [n.id];
            } else {
                clusters[n.metatype][n.cluster].push(n.id);
            }
        }
    }


    var mdl_rows = [];
    var idx = 0;

    $.each(clusters, function (k, v) {
        $.each(clusters[k], function (kk, vv) {

            mdl_rows.push({
                ids: vv,
                focused: false,
                group: k,
                name: "Cluster " + kk,
                size: vv.length,
                color: '<span id=\"meta_' + idx + '\" style="background-color:#ffffff" onclick="openModuleColorPicker(this);event.stopPropagation()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>'
                        // + Set2[kk] +
            });
            idx = idx + 1;

        });
    });

    $('#mdg').datagrid({
        onSelect: function (index, row) {
            //highlightCommunity(index, false);
            var e = cluster_arr[index];
            if (e !== undefined) {
                //e.focused = false
                //selectCluster(e)
            }

            //updateCellColor(highlightColor, "module_" + index);

        },
        onUnselect: function (index, row) {
            //resetCommunity(index);
            current_fun_nodes = null;
            var e = cluster_arr[index];
            if (e !== undefined) {
                // e.focused = true
                //selectCluster(e)
            }

            //updateCellColor("0xffffff", "module_" + index);
        }
    }).datagrid('loadData', mdl_rows);
    cluster_arr = mdl_rows
}

function selectCluster(e) {
    console.log("displayNodeInfo << --- 3");
    if (!e.focused) {
        e.focused = true
        var i = 0;
        var n;
        //var narr = e.userData.ids

        var arr = [];
        var count = 0
        for (var propertyNm in boundingClusters) {
            var obj = boundingClusters[propertyNm];
            if (obj.focused && count === 0) {
                arr = obj.userData.ids
                count++
            } else if (obj.focused) {
                arr = arr.concat(obj.userData.ids)
            }
        }

        Scatter.graphData().nodes.forEach(function (nd) {
            if (arr.indexOf(nd.id) === -1) {
                nd.highlight = 0
                colorNodeObj(nd, "#d3d3d3")
                opaNodeObj(nd, 0.2)
                deleteSprite(nd)
                deleteOutline(nd)
                if (nd.outline === 1) {
                    nd.outline = -1;
                }
                //nd.module = 2
            } else {
                nd.highlight = 1
                colorNodeObj(nd, nd.colorb)
                opaNodeObj(nd, 1)
                if (nd.labeled === 0) {
                    addLabel(nd)
                }
                if (nd.outline === -1) {
                    outlineNode(nd, highlightColor)
                }
            }
        })

        //Scatter.cooldownTicks(100)

        for (var propertyNm in boundingClusters) {
            if (!boundingClusters[propertyNm].focused) {
                var colorValue = parseInt("#d6d6d6".replace("#", "0x"), 16);
                var colored = new THREE.Color(colorValue);
                boundingClusters[propertyNm].material.color = colored
                boundingClusters[propertyNm].material.opacity = boundingClusters[propertyNm].material.opacity - 0.3
            } else {
                boundingClusters[propertyNm].visible = true
                boundingClusters[propertyNm].material.opacity = boundingClusters[propertyNm].userData.opacity
                boundingClusters[propertyNm].material.color = boundingClusters[propertyNm].userData.tcolor
            }
        }

        Scatter.controls().target.set(e.userData.centroid.x, e.userData.centroid.y, e.userData.centroid.z)
        currentOrigin = v(e.userData.centroid.x, e.userData.centroid.y, e.userData.centroid.z);
        displayNodeInfoModule(propertyNm + "");
    } else {
        e.focused = false;
        var arr = [];
        for (var propertyNm in boundingClusters) {
            var obj = boundingClusters[propertyNm];
            if (obj.focused) {
                arr = arr.concat(obj.userData.ids)
            }
        }

        Scatter.graphData().nodes.forEach(function (nd) {
            if (arr.length > 0) {
                if (arr.indexOf(nd.id) !== -1) {
                    nd.highlight = 1
                    colorNodeObj(nd, nd.tcolor)
                    opaNodeObj(nd, 1)
                    if (nd.labeled === 0) {
                        addLabel(nd)
                    }
                    if (nd.outline === -1) {
                        outlineNode(nd, highlightColor)
                    }
                } else {
                    nd.highlight = 0
                    colorNodeObj(nd, "#d3d3d3")
                    opaNodeObj(nd, 0.2)
                    deleteSprite(nd)
                    deleteOutline(nd)
                    if (nd.outline === 1) {
                        nd.outline = -1;
                    }
                }
            } else {
                nd.highlight = 0
                colorNodeObj(nd, nd.tcolor)
                opaNodeObj(nd, 1)
                if (nd.labeled === 0) {
                    addLabel(nd)
                }
                if (nd.outline === -1) {
                    outlineNode(nd, highlightColor)
                }
            }
        })
        //Scatter.cooldownTicks(100)
        for (var propertyNm in boundingClusters) {
            if (arr.length > 0) {
                if (boundingClusters[propertyNm].focused) {
                    boundingClusters[propertyNm].visible = true
                    boundingClusters[propertyNm].material.opacity = boundingClusters[propertyNm].userData.opacity
                    boundingClusters[propertyNm].material.color = boundingClusters[propertyNm].userData.tcolor
                } else {
                    var colorValue = parseInt("#d6d6d6".replace("#", "0x"), 16);
                    var colored = new THREE.Color(colorValue);
                    boundingClusters[propertyNm].material.color = colored
                    boundingClusters[propertyNm].material.opacity = boundingClusters[propertyNm].material.opacity - 0.3
                }
            } else {
                boundingClusters[propertyNm].visible = true
                boundingClusters[propertyNm].material.opacity = boundingClusters[propertyNm].userData.opacity
                boundingClusters[propertyNm].material.color = boundingClusters[propertyNm].userData.tcolor
            }
        }
        Scatter.controls().target.set(0, 0, 0)
        currentOrigin = v(0, 0, 0);
        displayNodeInfoModule(propertyNm + "");
    }
}

function updateMetaInfo(index, row) {
    current_selected_row = row
    openModuleColorPickerAll(row, index, "meta_")
    document.getElementById('selectId').innerHTML = current_selected_row.name;
}

function updateCustomInfo(index, row) {
    current_selected_row = row
    openModuleColorPickerAll(row, index, "cluster_")
    document.getElementById('selectId').innerHTML = current_selected_row.name;
}

function getBoxPlot(id) {
    var selected_grp;
    var selected_factor;
    var opt = $('#compopt').val();
    if (opt === "cluster") {
        selected_factor = "cluster"
        curr_meta = "cluster"
        var rows = $('#mdg').datagrid('getSelections');
        var selectedMeta = []
        for (var i = 0; i < rows.length; i++) {
            selectedMeta.push(rows[i].name.split("Cluster ")[1]);
        }
    } else {
        curr_meta = $("#metaopt").val();
        var rows = $('#metadg').datagrid('getSelections');
        var selectedMeta = [];
        for (var i = 0; i < rows.length; i++) {
            selectedMeta.push(rows[i].name);
        }
    }
    selected_factor = curr_meta;
    for (var i = 0; i < selectedMeta.length; i++) {
        if (i === 0) {
            selected_grp = selectedMeta[i]
        } else {
            selected_grp = selected_grp + "; " + selectedMeta[i]
        }
    }
    $.ajax({
        beforeSend: function () {
            $.messager.progress({
                text: 'Processing .....'
            });
        },
        dataType: "html",
        type: "POST",
        url: '/MetaboAnalyst/faces/AjaxCall',
        data: {function: 'plotSelectedGene', id: id, meta: selected_factor, selected: selected_grp},
        async: false,
        success: function (result) {
            $.messager.progress('close');
            $("#stats").empty();
            var boxplot = $("#dragDiv");
            boxplot.css('display', 'block')
            document.getElementById('image').src = usr_dir + "/" + result;
        }
    });
}

function updateSampleBasedOnLoading(id) {

    var omicstype = "NA"
    if (["mcia", "procrustes", "mbpca", "diablo", "spls", "rcca"].indexOf(gData.reductionOpt) !== -1) {
        omicstype = comparison_omicstype;
        if (omicstype === "omics") {
            $.messager.alert('', "Please select another meta data other than omics type!", 'error');
            return;
        }
    }
    $.ajax({
        beforeSend: function () {

        },
        dataType: "html",
        type: "POST",
        url: '/MetaboAnalyst/faces/AjaxCall',
        data: {function: 'updateSampleBasedOnLoading', id: id, omicstype: omicstype},
        async: false,
        success: function (result) {
            $.getJSON(usr_dir + "/" + result, function (jsonDat) {
                if (current_main_plot === "score") {

                    if (omicstype !== "NA") {
                        var i = 0;
                        if (Scatter.graphData().nodes.length === jsonDat.length) {
                            Scatter.graphData().nodes.forEach(function (n) {
                                if (i < jsonDat.length) {
                                    colorNodeObj(n, jsonDat[i]);
                                    i++;
                                }
                            })
                        } else {
                            Scatter.graphData().nodes.forEach(function (n) {
                                if (n.type === omicstype) {
                                    colorNodeObj(n, jsonDat[i]);
                                    i++;
                                } else {
                                    colorNodeObj(n, "#d3d3d3");
                                    opaNodeObj(n, 0.3)
                                }
                            })
                        }
                    } else {
                        var i = 0;
                        Scatter.graphData().nodes.forEach(function (n) {
                            if (i < jsonDat.length) {
                                colorNodeObj(n, jsonDat[i]);
                                i++;
                            }
                        })
                    }
                    Scatter.graphData().links.forEach(function (l) {
                        l.highlight = 0
                        l.width = 0
                        colorLineObj2(l, edgeColor)
                        sizeLineObj(l, 0)
                    })
                } else {
                    var i = 0;
                    scene2.traverse(function (child) {
                        if (child instanceof THREE.Mesh) {
                            if (child.userData.nodeData !== undefined) {
                                colorNodeObjScene2(child, jsonDat[i])
                                i++;
                            }
                        }
                    });
                }
            })
        }
    });
}

function computeEncasing(ids, type, color, group = "NA", level = 0.5, alphaVal = 0.5) {
    if (ids.length < 10 && type === "contour") {

        $.messager.alert('Error', 'At least 10 data points are required for computing density contour!', 'error');
        return;
    }
    var ids_string = ids[0];
    for (var i = 1; i < ids.length; i++) {
        ids_string = ids_string + "; " + ids[i]
    }
    var omicstype = "NA";
    if (isReductOptSingle || $("#loadingOpt").val().includes("pca_")) {
        omicstype = $("#loadingOpt").val();
    }

    $.ajax({
        beforeSend: function () {
            /*$.messager.progress({
             text: 'Processing .....'
             });*/
        },
        dataType: "html",
        type: "POST",
        url: '/MetaboAnalyst/faces/AjaxCall',
        data: {function: 'computeEncasing', type: type, names: ids_string, level: level, omics: omicstype},
        async: false,
        success: function (result) {
            $.getJSON(usr_dir + "/" + result, function (jsonDat) {
                var sc;
                sc = Scatter.scene()
                if (type === "ellipse" || type === "alpha") {
                    gData.ellipse = jsonDat;
                    computeEllipsoid(sc, ids, color, type, group, alphaVal)
                } else {
                    gData.objects = jsonDat;
                    computeContours(sc, ids, color, group, alphaVal)
                }

            })
            $("#loader").hide()
        },
        error: function () {
            $.messager.alert('Error', 'Failed to process the request!', 'error');
            $.messager.progress('close');
        }
    });
}

function updateClusterInfo(index, row) {
    current_selected_row = row.name
    openModuleColorPickerAll(row, index, "meta_")
    var e = cluster_arr[index];
    if (e !== undefined) {
        cluster_arr[index].focused = false
        selectClusterNew(e)
    }

    updateCellColor(highlightColor, "meta_" + index);
}

function displayPatternData(row, type) {
    $("#dragDiv").css('display', 'none')
    //$('#p').panel('open')
    var stats = $("#stats");
    var title = row.name
    var nodes = [];

    if (type === "metatype" || type === "meta") {
        Scatter.graphData().nodes.forEach(function (n) {
            if (n[type] === row.name) {
                nodes.push(n)
            }
        });
    } else {
        Scatter.graphData().nodes.forEach(function (n) {
            if (n[type] === row.index + 1) {
                nodes.push(n)
            }
        });
    }

    stats.empty();
    if (title !== "") {
        stats.append('<lh><b>' + title + '</b></lh>');
    }
    for (var i = 0; i < nodes.length; i++) {
        stats.append('<li class="stats">' + nodes[i].label + '</li>');
    }
}

function disposeMesh(mesh, scene) {
    if (mesh.material !== undefined) {
        mesh.visible = false
        mesh.scale.set(1, 1, 1)
        mesh.material.dispose();
        mesh.geometry.dispose();
        mesh.geometry = undefined;
        mesh.material = undefined;
        scene.remove(mesh);
    }
}

function deleteMetaSphere(inx, scene) {
    if (boundingClusters[inx] !== undefined) {
        var mesh = boundingClusters[inx];
        mesh.visible = false
        mesh.scale.set(1, 1, 1)
        scene.remove(mesh);
        mesh.material.dispose();
        mesh.geometry.dispose();
        mesh.geometry = undefined;
        mesh.material = undefined;
        delete boundingClustersGroup[inx];
        delete boundingClusters[inx];
        delete activeModules[inx];
    }
}

function generateAxisLabel(type, scene) {
    var type = $('#axisType').val();    
    var length = 1100;
    var xString = "Comp.1";
    var yString = "Comp.2";
    var zString = "Comp.3";

    //labelArr = {};

    for (var propNm in gData.misc) {

        if (propNm === "pct" && !currentOmicsType.includes("pca_")) {
            xString = "Comp.1 (" + gData.misc[propNm][0] + "%)";
            yString = "Comp.2 (" + gData.misc[propNm][1] + "%)";
            zString = "Comp.3 (" + gData.misc[propNm][2] + "%)";
            break;
        } else if (propNm === "pct2") {
            if (currentOmicsType.includes("pca_")) {
                xString = "Comp.1 (" + gData.misc.pct2[currentOmicsType][0] + "%)";
                yString = "Comp.2 (" + gData.misc.pct2[currentOmicsType][1] + "%)";
                zString = "Comp.3 (" + gData.misc.pct2[currentOmicsType][2] + "%)";
                break;

            } else if (currentOmicsType !== "" && currentOmicsType !== "both") {
                xString = "Comp.1 (" + gData.misc.pct2[currentOmicsType][0] + "%)";
                yString = "Comp.2 (" + gData.misc.pct2[currentOmicsType][1] + "%)";
                zString = "Comp.3 (" + gData.misc.pct2[currentOmicsType][2] + "%)";
                break;

            } else {
                var keyNames = Object.keys(gData.misc.pct2);
                xString = "Comp.1 (" + gData.misc.pct2[keyNames[0]][0] + "%)";
                yString = "Comp.2 (" + gData.misc.pct2[keyNames[0]][1] + "%)";
                zString = "Comp.3 (" + gData.misc.pct2[keyNames[0]][2] + "%)";
                break;

            }
        }
    }

    var titleX = createText2D(xString, scene.uuid);
    scene.add(titleX);
    labelArr["x"] = titleX;

    var titleX = createText2D(yString, scene.uuid);
    scene.add(titleX);
    labelArr["y"] = titleX;

    if (!twoDMode) {
        var titleZ = createText2D(zString, scene.uuid);
        scene.add(titleZ);
        labelArr["z"] = titleZ;
    } else {
        myTexts["Z" + "" + scene.uuid].visible = false;
        delete myTexts["Z" + "" + scene.uuid];
        scene.remove(myTexts["Z" + "" + scene.uuid]);
    }

    if (!twoDMode) {
        if (type === "bottom") {
            labelArr["x"].position.set(length + 40, -length + 20, -length + 20);
            labelArr["y"].position.set(-length + 40, length + 20, -length + 20);
            labelArr["z"].position.set(-length + 40, -length + 20, length + 20);

        } else {
            labelArr["x"].position.set(length * 1.1, 0, 0);
            labelArr["y"].position.set(0, length * 1.1, 0);
            labelArr["z"].position.set(0, 0, length * 1.1);
        }
    } else {
        if (type === "bottom") {
            labelArr["x"].position.set(length + 40, -length + 20, 0);
            labelArr["y"].position.set(-length + 40, length + 20, 0);
        } else {

            labelArr["x"].position.set(length * 1.1, 0, 0);
            labelArr["y"].position.set(0, length * 1.1, 0);
        }
    }

}

function removeAxes(scene) {
    if (axesArr[scene.uuid] !== undefined) {
        for (var propertyNm in axesArr[scene.uuid]) {
            var pl = axesArr[scene.uuid][propertyNm]

            disposeMesh(pl.line, scene);
            disposeMesh(pl.cone, scene);
            pl = undefined;
            axesArr[scene.uuid][propertyNm] = undefined;
            scene.remove(axesArr[scene.uuid][propertyNm]);
            delete axesArr[scene.uuid][propertyNm]
        }
    }
}

function generateAxes(scene) {
    
    var type = $("#axisType").val();
    var textDisplay = $("#labelOpt").val();
    var length = 1100;
    var dir = new THREE.Vector3(1, 0, 0);
    var origin = new THREE.Vector3(-length, 0, 0);
    var colorValue = parseInt(axisColorU.replace("#", "0x"), 16);
    var colored = new THREE.Color(colorValue);
//    var axisColorUx = "#c43535";
//    var colorValue = parseInt(axisColorUx.replace("#", "0x"), 16);
//    var colored = new THREE.Color(colorValue);

    if (axesArr[scene.uuid] !== undefined) {
        for (var propertyNm in axesArr[scene.uuid]) {
            var pl = axesArr[scene.uuid][propertyNm];
            disposeMesh(pl.line, scene);
            disposeMesh(pl.cone, scene);
            pl = undefined;
            axesArr[scene.uuid][propertyNm] = undefined;
            scene.remove(axesArr[scene.uuid][propertyNm]);
            delete axesArr[scene.uuid][propertyNm];
        }
    }
    axesArr[scene.uuid] = {};
   
    var arrow = new THREE.ArrowHelper(dir, origin, 2 * length, colored, 0.1 * length, 0.4 * 0.1 * length);
    var arrow2 = new THREE.ArrowHelper(v(0, 1, 0), v(0, -length, 0), 2 * length, colored, 0.1 * length, 0.4 * 0.1 * length);
    var arrow3 = new THREE.ArrowHelper(v(0, 0, 1), v(0, 0, -length), 2 * length, colored, 0.1 * length, 0.4 * 0.1 * length);
    scene.add(arrow);
    scene.add(arrow2);

    axesArr[scene.uuid]["x"] = arrow;
    axesArr[scene.uuid]["y"] = arrow2;
    if (!twoDMode) {
        axesArr[scene.uuid]["z"] = arrow3;
        scene.add(arrow3);
        arrow3 = axesArr[scene.uuid]["z"];
    }

    arrow = axesArr[scene.uuid]["x"];
    arrow2 = axesArr[scene.uuid]["y"];

    if (type === "bottom") {
        if (!twoDMode) {
            arrow.position.set(-length, -length, -length);
            arrow2.position.set(-length, -length, -length);
            arrow3.position.set(-length, -length, -length);
        } else {
            arrow.position.set(-length, -length, 0);
            arrow2.position.set(-length, -length, 0);
            arrow3.position.set(-length, -length, 0);
        }
    } else {
        if (!twoDMode) {
            arrow.position.set(-length, 0, 0);
            arrow2.position.set(0, -length, 0);
            arrow3.position.set(0, 0, -length);
        } else {
            arrow.position.set(-length, 0, 0);
            arrow2.position.set(0, -length, 0);
        }

    }
    generateAxisLabel(type, scene);
    var sceneCenter = v(0, 0, 0);
    var type1 = $("#axisType").val();
    if (twoDMode) {
        sceneCenter = v(-length, -length, 0);
    } else if (type1 === "bottom") {
        sceneCenter = v(-length, -length, -length);
    }
    for (var propertyNm in boundingClustersGroup) {
        var obj = boundingClustersGroup[propertyNm];
        obj.clusterNum = propertyNm;
        var clusterCenter = obj.centroid;
        var dir = new THREE.Vector3(); // create once an reuse it
        dir.subVectors(clusterCenter, sceneCenter).normalize();

        // scalar to simulate speed
        var speed = 0.05;
        var vector = dir.multiplyScalar(1000, 1000, 1000);
        obj.explosionDirection = vector;
        obj.targetPosition = new THREE.Vector3();
        obj.targetPosition.x += obj.explosionDirection.x;
        obj.targetPosition.y += obj.explosionDirection.y;
        obj.targetPosition.z += obj.explosionDirection.z;
        obj.targetPositionCopy = JSON.parse(JSON.stringify(obj.targetPosition));

        Scatter.graphData().nodes.forEach(function (n) {
            if (obj.userData.indexOf(n.id) !== -1) {
                //n.explosionDirection = obj.explosionDirection
                //obj.add(n.__threeObj)
            }
        });
    }
}

function initPlane(scene) {
    var colorValue = parseInt(layerCol.replace("#", "0x"), 16);
    var colored = new THREE.Color(colorValue);
    if (planeU[scene.uuid] !== undefined) {
        disposeMesh(planeU[scene.uuid], scene);
        planeU[scene.uuid] = undefined;
    }

    var renderer = Scatter.renderer()
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;

    var geometry = new THREE.PlaneGeometry(7000, 7000, 32);
    var material = new THREE.MeshStandardMaterial({color: colored, side: THREE.DoubleSide});
    planeU[scene.uuid] = new THREE.Mesh(geometry, material);
    planeU[scene.uuid].userData = "floor"
    planeU[scene.uuid].receiveShadow = true
    planeU[scene.uuid].position.set(0, -1500, 0)
    planeU[scene.uuid].lookAt(0, 0, 0)

    scene.add(planeU[scene.uuid]);

}

function updateAxes() {
    var opt = $('#axisOpt').val();
    var col = axisColorU;
    if (mainCheckBool) {
        generateAxes(Scatter.scene());
    } else {
        generateAxes(scene2);
    }

}

function updateCustomSelection() {
    var haloBool = $("#haloCheckbox").is(':checked');
    var clusterBool = $("#clusterCheckbox").is(':checked');
    var col = current_point_color;
    var colorValue = parseInt(col.replace("#", "0x"), 16);
    var coll = new THREE.Color(colorValue);

    var curType = current_module.split(/_(.+)/)[0]
    curType = curType.replace("overall", "meta")
    curType = curType.replace("targeted", "cluster")
    var curSelection = current_module.split(/_(.+)/)[1]
    current_module = curType + "_" + curSelection
    updateCellColor(col, current_module)
    var curShape = $("#shapeOpt").val()
    var sizeval = parseInt($("#nsize").val());
    var row;

    row_selection_on_user_click = false
    $('#metadg').datagrid('selectRow', curSelection);
    row = $('#metadg').datagrid('getRows')[curSelection];
    var curr_meta = row.name;
    highlightSelectedMeta(curr_meta, col, haloBool, $('#encasingType').val());

    changeShapeSelectedMeta(curr_meta, curShape)
    if (haloBool) {
        updateCellColor(current_halo_color, "metahalo_" + curSelection)
    } else {
        updateCellColor("#ffffff", "metahalo_" + curSelection)
    }

    row_selection_on_user_click = true;
    updateLabel();

    document.getElementById('clusterRow').style.display = "none";

    if (haloBool) {
        row.selectionSettings.haloBool = true
        row.selectionSettings.haloColor = current_halo_color
    } else {
        row.selectionSettings.haloBool = false
    }

    if ($('#encasingType').val() !== "na") {
        row.selectionSettings.boundary = $('#encasingType').val()
        row.selectionSettings.boundaryColor = current_encasing_color
    } else {
        row.selectionSettings.boundary = "na"
    }
    row.selectionSettings.nodeSize = sizeval;

    $("#customClusterOpt").val("NA");
    $('#modulecolordlg').dialog('close');

}

function highlightCommunity(inx, meta) {
    var inx = inx + 1;
    var id_sbl_path = sub_modules[inx].split(";");
    var ids = id_sbl_path[3].split("->");
    var community = [];
    highlight_mode = 1;
    var nodes = [];
    var nodeids = []
    if (!meta) {
        var coll = $("#module_" + inx).css("background-color");
        var a = coll.split("(")[1].split(")")[0];
        a = a.split(",");
        var col = convertToHex(a)
        if (col === "000000") {
            col = highlightColor
        }
    } else {
        col = highlightColor
    }
    Scatter.graphData().nodes.forEach(function (n) {
        if (ids.indexOf(n.id) !== -1) {
            if (!meta) {
                if (highlightType === "mixed") {
                    if (curr_mode !== "metasphere") {
                        colorNodeObj(n, col)
                        n.highlight = 1;
                    }
                    //n.color = highlightColor;

                } else {
                    highlightNode(n);
                }

                community.push({id: n.id, label: n.label});
            }
            nodes.push(n)
            nodeids.push(n.id)
        } else {
            if (!n.highlight === 0) { //other community
                n.color = greyColor;
            }
        }
    });

    if (meta) {
        addMetaSphereExperimental(nodes, inx);
    } else {
        var vertices = [];
        Scatter.graphData().nodes.forEach(function (n) {
            if (nodeids.indexOf(n.id) !== -1) {
                var pos = new THREE.Vector3()
                pos.x = n.x
                pos.y = n.y
                pos.z = n.z
                vertices.push(pos)
            }

        })
        if (curr_mode !== "random" || Scatter.numDimensions() !== 2) {
            var sphere = new THREE.Sphere().setFromPoints(vertices);
            const distRatio = 1 + 300 / Math.hypot(sphere.center.x, sphere.center.y, sphere.center.z);
            Scatter.cameraPosition(
                    {x: sphere.center.x * distRatio, y: sphere.center.y * distRatio, z: sphere.center.z * distRatio}, // new position
                    {x: 0, y: 0, z: 0}, // lookAt ({ x, y, z })
                    2000  // ms transition duration
                    );
        }
        highlightDirectLinks();
    }

    current_fun_nodes = ids;
    displayCurrentSelectedNodes(community, "");
}

function resetCommunity(inx) {
    var id_sbl_path = sub_modules[inx].split(";");
    var ids = id_sbl_path[3].split("->");

    var count = 0;
    Scatter.graphData().nodes.forEach(function (n) {
        if (ids.indexOf(n.id) !== -1) {
            if (highlightType === "mixed") {
                n.highlight = 0;
                if (n.prevCol === undefined) {
                    colorNodeObj(n, n.tcolor);
                    n.color = n.tcolor;
                } else {
                    colorNodeObj(n, n.prevCol);
                    n.color = n.prevCol;
                }
            } else {
                unhighlightNode(n);
            }
        }
        if (n.highlight === 1) {
            count++;
        }
    });
    setEdgeColorGrey()
    $("#stats").empty();
}

function changeShapeCommunity(inx, shape) {
    var ids = boundingClustersGroup[inx].userData;
    Scatter.graphData().nodes.forEach(function (n) {
        if (ids.indexOf(n.id) !== -1) {
            shapeNodeObj(n, shape)
        }
    })
}

function changeShapeSelectedMeta(name, shape) {

    Scatter.graphData().nodes.forEach(function (n) {
        if (n.metatype === name) {
            n.highlight = 1
            shapeNodeObj(n, shape)
        }
    })
}

function initScatter2() {
    var light = new THREE.AmbientLight(0x404040, 0.3); // soft white light
    scene2.add(light);

    nav_arr.forEach(function (n) {
        disposeMesh(n, scene2)
    });
    nav_arr = []

    scene2.background = new THREE.Color(0x003366);

    if (gData.edgesMain !== null && gData.edgesMain !== undefined) {
        gData.edgesMain.forEach(function (e) {
            scene2.add(e.__lineObj);
            nav_arr.push(e.__lineObj)
        });
    }

    gData.navigation.forEach(function (n) {
        var res = 0;
        if (n.tsize < 8) {
            res = 6;
        } else if (n.tsize < 10) {
            res = 12;
        } else {
            res = 16
        }

        if (n.meta !== "mcia.seg") {
            if (["mcia", "procrustes"].indexOf(gData.reductionOpt) !== -1) {
                var geom = new THREE.SphereBufferGeometry(Math.cbrt(1) * 1.2 * 15, res, res / 2);

            } else {

                var geom = new THREE.SphereBufferGeometry(Math.cbrt(1) * 15, res, res / 2);
            }
            var col;
            //if (gData.navigation.length > 200 && gData.objects !== "NA") {
            //    col = boundingClusters[n.cluster].userData.tcolor
            //} else {
            col = n.colorb
            //}
            var mat = new THREE.MeshPhongMaterial({
                color: col,
                transparent: true,
                opacity: 0.6,
                reflectivity: 0.5,
                emissive: 0x000000,
                depthWrite: false,
                depthTest: false
            })


            var mesh = new THREE.Mesh(geom, mat)

            if (n.tsize < 7.5) {
                mesh.visible = false
            }

            sizeMeshObj(mesh, n.tsize * 0.1)
            mesh.position.set(n.fx, n.fy, n.fz)
            mesh.userData.nodeData = n;

            nav_arr.push(mesh)
            nav_nodes_arr.push(n)
            scene2.add(mesh);
        }
    });

    scene2.traverse(function (child) {
        if (child instanceof THREE.Mesh) {
            if (child.userData.nodeData !== undefined) {
                if (child.userData.nodeData.color !== undefined && child.userData.nodeData.color !== null) {
                    var colorValue = parseInt(child.userData.nodeData.color.replace("#", "0x"), 16);
                } else {
                    var colorValue = parseInt(child.userData.nodeData.topocolb.replace("#", "0x"), 16);
                }
                //if (initScene2) {
                //  var col = new THREE.Color(0xd3d3d3);
                //} else {
                var col = new THREE.Color(colorValue);
                //}
                child.material.color = col;
                child.material.opacity = 0.8;
                child.visible = true;
            }
        }
    });
    initScene2 = false;
}

function sizeMeshObj(mesh, size) {
    const object = mesh
    if (object !== null && object !== undefined) {
        object.scale.set(size, size, size);
    }
}

// Useful, keep!
function turnOffFloor(off) {
    var sceneArr = [Scatter.scene(), scene2]
    for (var i = 0; i < sceneArr.length; i++) {
        var sc = sceneArr[i]
        sc.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (child.userData === "floor") {
                    if (off) {
                        child.visible = false;
                    } else {
                        child.visible = true;
                    }
                }
            }
        });
    }
}

function colorNodeObjScene2(object, color) {
    object.userData.nodeData.color = color.replace("0x", "#");
    var colorValue = parseInt(color.replace("#", "0x"), 16);
    var col = new THREE.Color(colorValue);
    if (object !== null && object !== undefined) {
        const clonedMaterial = object.material.clone();
        clonedMaterial.setValues({color: col});
        object.material = clonedMaterial;
    }
}

function changePlaneType() {
    var mainCheck = document.getElementById("mainCheck");
    var sc;
    if (mainCheckBool) {
        sc = Scatter.scene();
    } else {
        sc = scene2
    }
    initPlane(sc);
}

function searchNavigationScatter(id) {

    scene2.traverse(function (child) {
        if (child instanceof THREE.Mesh) {
            if (child.userData.nodeData !== undefined) {
                if (child.userData.nodeData.id === id || child.userData.nodeData.label === id) {
                    //shiftCameraToNode(child.userData.nodeData);
                    highlightNavigationNode(id)
                }
            }
        }
    });
}

function highlightNavigationNode(id) {
    scene2.traverse(function (child) {
        if (child instanceof THREE.Mesh) {
            if (child.userData.nodeData !== undefined) {
                if (child.userData.nodeData.id === id || child.userData.nodeData.label === id) {
                    child.userData.nodeData.highlight = 1;
                    var colorValue = parseInt(highlightColor.replace("#", "0x"), 16);
                    var col = new THREE.Color(colorValue);
                    child.material.color = col;
                    child.material.opacity = 0.6;
                    child.visible = true;
                } else {
                    //if(child.userData.nodeData.highlight !== 1){
                    child.material.transparent = true;
                    child.material.opacity = 0.15;
                    //}
                    var colorValue = parseInt("0xd3d3d3".replace("#", "0x"), 16);
                    var col = new THREE.Color(colorValue);
                    child.material.color = col;

                }

            }
        }
    });
}

function unhighlightNavigationNode(id) {
    scene2.traverse(function (child) {
        if (child instanceof THREE.Mesh) {
            if (child.userData.nodeData !== undefined) {
                if (child.userData.nodeData.id === id) {
                    var colorValue = parseInt(child.userData.nodeData.colorb.replace("#", "0x"), 16);
                    var col = new THREE.Color(colorValue);
                    child.material.color = col;
                    child.material.opacity = 0.6;
                } else {
                    if (nav_nodes_arr.length > 500) {
                        child.material.transparent = false;
                        child.material.opacity = 0.6;
                        var colorValue = parseInt(child.userData.nodeData.colorb.replace("#", "0x"), 16);
                        var col = new THREE.Color(colorValue);
                        child.material.color = col;
                    }
                }
            }
        }
    });
}

function deleteClusteringShapes(scene) {
    var nms = Object.keys(boundingClusters)
    for (var i = 0; i < nms.length; i++) {
        var nm = nms[i]
        deleteMetaSphere(nm, scene);
    }
}

function selectClusterScene2(e) {
    if (!e.focused) {
        e.focused = true
        var i = 0;
        var n;
        //var narr = e.userData.ids
        var arr = []
        //for (var i = 0; i < cluster_arr.length; i++) {
        //    var obj = cluster_arr[i];
        //    if (obj.focused) {
        arr = arr.concat(e.ids)
        //    }
        //}

        scene2.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (child.userData.nodeData !== undefined) {
                    if (arr.indexOf(child.userData.nodeData.id) !== -1) {
                        var colorValue = parseInt(highlightColor.replace("#", "0x"), 16);
                        var col = new THREE.Color(colorValue);
                        child.material.color = col;
                        child.material.opacity = 1;
                    }
                }
            }
        });

    } else {
        e.focused = false;
        var arr = [];
        /*
         for (var i = 0; i < cluster_arr.length; i++) {
         var obj = cluster_arr[i];
         if (obj.focused) {
         arr = arr.concat(obj.ids)
         }
         }*/
        arr = arr.concat(e.ids)

        scene2.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (child.userData.nodeData !== undefined) {
                    if (arr.length > 0) {
                        if (arr.indexOf(child.userData.nodeData.id) !== -1) {
                            var colorValue = parseInt(child.userData.nodeData.colorb.replace("#", "0x"), 16);
                            var col = new THREE.Color(colorValue);
                            child.material.color = col;
                            child.material.opacity = 1;
                        }
                    } else {
                        var colorValue = parseInt(child.userData.nodeData.colorb.replace("#", "0x"), 16);
                        var col = new THREE.Color(colorValue);
                        child.material.color = col;
                        child.material.opacity = 1;
                    }
                }
            }
        });


        Scatter.graphData().nodes.forEach(function (nd) {
            if (arr.length > 0) {

                if (arr.indexOf(nd.id) !== -1) {
                    // nd.highlight = 1
                    colorNodeObj(nd, nd.tcolor)
                    opaNodeObj(nd, 1)
                    deleteOutline(nd)
                    if (nd.outline === -1) {
                        //outlineNode(nd, highlightColor)
                    }
                } else {

                }
            } else {
                /*
                 //nd.highlight = 1
                 colorNodeObj(nd, nd.tcolor)
                 opaNodeObj(nd, 1)
                 deleteOutline(nd)
                 if (nd.outline === -1) {
                 //outlineNode(nd, highlightColor)
                 }*/
            }
        })
    }
}

function selectClusterNew(e) {
    if (!e.focused) {
        e.focused = true
        var i = 0;
        var n;
        //var narr = e.userData.ids
        var arr = []
        //for (var i = 0; i < cluster_arr.length; i++) {
        //var obj = cluster_arr[i];
        //if (obj.focused) {
        arr = e.ids
        //}
        //}

        Scatter.graphData().nodes.forEach(function (nd) {
            if (arr.indexOf(nd.id) === -1) {

            } else {
                //nd.highlight = 1
                colorNodeObj(nd, highlightColor)
                opaNodeObj(nd, 1)
                if (nd.labeled === 0) {
                    addLabel(nd)
                }
                if (nd.outline === -1) {
                    //outlineNode(nd, highlightColor)
                }
            }
        })

    } else {
        e.focused = false;
        var arr = [];
        //for (var i = 0; i < cluster_arr.length; i++) {
        //var obj = cluster_arr[i];
        //if (obj.focused) {
        arr = e.ids
        //}
        //}


        Scatter.graphData().nodes.forEach(function (nd) {
            if (arr.length > 0) {
                if (arr.indexOf(nd.id) !== -1) {
                    // nd.highlight = 1
                    colorNodeObj(nd, nd.tcolor)
                    opaNodeObj(nd, 1)
                    deleteOutline(nd)
                    if (nd.outline === -1) {
                        //outlineNode(nd, highlightColor)
                    }
                } else {

                }
            } else {
                /*
                 //nd.highlight = 1
                 colorNodeObj(nd, nd.tcolor)
                 opaNodeObj(nd, 1)
                 deleteOutline(nd)
                 if (nd.outline === -1) {
                 //outlineNode(nd, highlightColor)
                 
                 }*/
            }
        })

    }
}

function computeAlpha(scene) {
    shape_type = "ellipse"
    var nms = Object.keys(boundingClusters)
    for (var i = 0; i < nms.length; i++) {
        var nm = nms[i]
        deleteMetaSphere(nm, scene);
    }
    var col;
    var mesh;
    for (var i = 0; i < gData.ellipse.length; i++) {
        var ob = gData.ellipse[i].vb
        var points = []
        var xArr = [];
        var yArr = [];
        var zArr = [];
        for (var j = 0; j < ob[0].length; j++) {
            points.push(v(ob[0][j] * 1000, ob[1][j] * 1000, ob[2][j] * 1000));
        }
        var geom = new THREE.ConvexGeometry(points)
        var opp = 0.7
        opp = opp + 0.3
        var tr = true;
        if (opp > 0.7) {
            opp = 0.7
            col = 0x431c53
        } else {
            if (opp > 0.6) {
                opp = 1
                col = 0xDA70D6
            } else {
                opp = 0.25
                col = 0xDA70D6
            }
            tr = true
        }
        var color = Set2[i]
        var material = new THREE.MeshPhongMaterial({color: color, transparent: true, alphaTest: 0.1, opacity: 0.4});
        mesh = new THREE.Mesh(geom, material)
        var nds = Scatter.graphData().nodes
        var nodes_vec = [];
        nds.forEach(function (n) {
            if (n.cluster === i + 1) {
                nodes_vec.push(n.id)
                xArr.push(n.fx)
                yArr.push(n.fy)
                zArr.push(n.fz)
            }
        })

        var meanX = meanF(xArr)
        var meanY = meanF(yArr)
        var meanZ = meanF(zArr)
        var meanXYZ = v(meanX, meanY, meanZ)
        //geom.mergeVertices();
        //geom.computeVertexNormals()
        var clusterGroup = new THREE.Object3D();
        mesh.userData.ids = nodes_vec;
        mesh.userData.centroid = meanXYZ
        mesh.userData.shadow = true
        mesh.castShadow = false
        mesh.receiveShadow = false
        //mesh.scale.set(1000, 1000, 1000)
        mesh.focused = false;
        var colorValue = parseInt(color.replace("#", "0x"), 16);
        var colored = new THREE.Color(colorValue);
        mesh.userData.tcolor = colored
        mesh.userData.tcolorhex = color
        mesh.userData.opacity = opp
        clusterGroup.userData = nodes_vec;
        clusterGroup.centroid = meanXYZ
        //mesh.scale.set(1000, 1000, 1000)
        mesh.position.set(meanX, meanY, meanZ)
        clusterGroup.focused = false;
        clusterGroup.add(mesh)
        scene.add(clusterGroup);
        boundingClusters[i + 1] = mesh;
        boundingClustersGroup[i + 1] = clusterGroup;
    }
    var sceneCenter = v(0, 0, 0)
    for (var propertyNm in boundingClustersGroup) {
        var obj = boundingClustersGroup[propertyNm];
        obj.clusterNum = propertyNm
        var clusterCenter = obj.centroid
        var dir = new THREE.Vector3(); // create once an reuse it
        dir.subVectors(clusterCenter, sceneCenter).normalize();

        // scalar to simulate speed
        var speed = 0.05;
        var vector = dir.multiplyScalar(1000, 1000, 1000);
        obj.explosionDirection = vector;
        obj.targetPosition = new THREE.Vector3()
        obj.targetPosition.x += obj.explosionDirection.x;
        obj.targetPosition.y += obj.explosionDirection.y;
        obj.targetPosition.z += obj.explosionDirection.z;
        obj.targetPositionCopy = JSON.parse(JSON.stringify(obj.targetPosition));

        Scatter.graphData().nodes.forEach(function (n) {
            if (obj.userData.indexOf(n.id) !== -1) {
                //n.explosionDirection = obj.explosionDirection
                //obj.add(n.__threeObj)
            }
        })
    }
}

function deleteOutlineScene2(object) {
    var n = object.userData.nodeData;
    if (outlineUIs[n.id] !== undefined) {
        var group = object
        for (var i = 0; i < group.children.length; i++) {
            var child = group.children[i]
            if (child.userData.type === "halo") {
                group.remove(child);
            }
        }

        //group.remove(group.children[group.children.length - 1]);
        var mesh = outlineUIs[n.id];
        scene2.remove(mesh);
        if (mesh) {
            if (mesh.material.geometry) {
                mesh.geometry.dispose();
                mesh.geometry = undefined;
            }
            if (mesh.material.map)
                mesh.material.map.dispose();
            if (mesh.material.lightMap)
                mesh.material.lightMap.dispose();
            if (mesh.material.bumpMap)
                mesh.material.bumpMap.dispose();
            if (mesh.material.normalMap)
                mesh.material.normalMap.dispose();
            if (mesh.material.specularMap)
                mesh.material.specularMap.dispose();
            if (mesh.material.envMap)
                mesh.material.envMap.dispose();
            mesh.material.dispose();
            mesh.material = undefined;
            mesh = undefined;
            outlineUIs[n.id] = null;
            delete outlineUIs[n.id];
        }
    }
}

function outlineNodeScene2(object, color) {
    object.userData.nodeData.color = color.replace("0x", "#");
    var n = object.userData.nodeData
    var colorValue = parseInt(color.replace("#", "0x"), 16);
    var col = new THREE.Color(colorValue);
    if (object !== null && object !== undefined) {
        const clonedMaterial = object.material.clone();
        clonedMaterial.setValues({color: col});
        object.material = clonedMaterial;
        if (n.outline === 1) {
            deleteOutlineScene2(n);
        }
        n.outline = 1
        var particle = new THREE.Sprite(new THREE.SpriteMaterial({
            map: new THREE.CanvasTexture(generateSprite(color)),
            alphaTest: 0.5,
            transparent: true,
            fog: true
        }));
        particle.scale.x = particle.scale.y = object.userData.scaling[0] * 3 + 115;
        var jsonKey = n.id;
        outlineUIs[jsonKey] = particle;
        outlineUIs[jsonKey].userData.type = "halo";
        outlineUIs[jsonKey].userData.id = n.id;
        outlineUIs[jsonKey].userData.color = color.replace("#", "0x");
        //particle.position.set(threeNodes[n.id].position.x, threeNodes[n.id].position.y, threeNodes[n.id].position.z)

        object.add(particle);
    }
}

function mergeClusters() {
    var rows = $('#mdg').datagrid('getChecked');
    if (rows.length < 2) {
        $.messager.alert('Error', 'At least two rows should be selected before using the merging function.', 'error');
        return;
    }
    var inxArr = []
    var idsArr = []
    var nmsArr = [];
    for (var i = 0; i < rows.length; i++) {
        inxArr.push(rows[i].index)
        idsArr.push(...rows[i].ids)
        nmsArr.push(rows[i].name)
    }

    var groupVal = 1

    var i = cluster_arr.length
    var lastMergedGroupInx = 0
    while (i--) {
        if (cluster_arr[i].group === "Merged") {
            lastMergedGroupInx = i
            groupVal++
        }
        if (inxArr.indexOf(i) !== -1) {
            cluster_arr.splice(i, 1)
        }
    }

    newlyMergedRow = {index: 0, ids: idsArr, group: "Merged", name: "Group " + groupVal, size: idsArr.length,
        color: '<span id=\"meta_' + 0 + '\" style="background-color:#ffffff" onclick="openModuleColorPicker(this);event.stopPropagation()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>',
        colorhalo: '<span id=\"metahalo_' + 0 + '\" style="background-color:#ffffff" onclick="openModuleColorPicker(this);event.stopPropagation()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>',
        child: nmsArr, groupVal: groupVal
    }
    cluster_arr.unshift(newlyMergedRow)

    for (var i = 0; i < cluster_arr.length; i++) {
        cluster_arr[i].index = i;
    }
    ;
    $('#mdg').datagrid('loadData', []);
    $('#mdg').datagrid('loadData', cluster_arr);
}

function deleteMesh(mesh, scene) {
    scene.remove(mesh)
    if (mesh.geometry) {
        mesh.geometry.dispose();
        mesh.geometry = undefined;
    }
    if (mesh.material) {
        if (mesh.material.map)
            mesh.material.map.dispose();
        if (mesh.material.lightMap)
            mesh.material.lightMap.dispose();
        if (mesh.material.bumpMap)
            mesh.material.bumpMap.dispose();
        if (mesh.material.normalMap)
            mesh.material.normalMap.dispose();
        if (mesh.material.specularMap)
            mesh.material.specularMap.dispose();
        if (mesh.material.envMap)
            mesh.material.envMap.dispose();
        mesh.material.dispose();
        mesh.material = undefined;
    }
    mesh = undefined;


}

function addTextNodeObj(n, sceneInt, type) { // 98 scene1/ 99 scene2
    const object = n.__threeObj
    if (object !== null && object !== undefined) {
        var sprite;
        var text = "";
        if (type === "name") {
            text = n.label;
        } else {
            text = n.metatype;
        }
        sprite = new SpriteText(text);
        sprite.color = n.color;
        sprite.textHeight = 30;
        sprite.fontSize = sceneInt;
        sprite.userData === "textAsNode"
        sprite.material.transparent = false;
        object.add(sprite);
    }
}

function addTextObj(object, sceneInt) { // 98 scene1/ 99 scene2
    if (object !== null && object !== undefined) {
        const sprite = new SpriteText(object.userData.nodeData.label);
        sprite.color = object.userData.nodeData.color;
        sprite.textHeight = 30;
        sprite.fontSize = sceneInt;
        sprite.userData === "textAsNode"
        sprite.material.transparent = false
        object.add(sprite);
    }
}

function removeTextNodeObj(object) {
    if (object !== null && object !== undefined) {
        object.traverse(function (child) {
            if (child._fontSize !== undefined) {
                if (child._fontSize === 48 || child._fontSize === 49) {
                    child.visible = false;
                    child.scale.set(1, 1, 1)
                    Scatter.scene().remove(child);
                }
            }
        });
    }
}

function setTextNodeSize(object, increaseBool) {
    if (object !== null && object !== undefined) {
        object.traverse(function (child) {
            if (child._fontSize !== undefined) {
                if (child._fontSize === 48 || child._fontSize === 49) {
                    if (increaseBool) {
                        child.scale.set(child.scale.x * 1.1, child.scale.y * 1.1, child.scale.z * 1.1);
                    } else {
                        child.scale.set(child.scale.x * 0.9, child.scale.y * 0.9, child.scale.z * 0.9);
                    }
                }
            }
        });
    }
}

function testEnrichment() {
    doEnrichmentTests(function (result) {
        if (result === 'NA.json') {
            $.messager.alert('Enrichment Error', 'No result has been found for the queried molecules in our database. Please try with another database or other molecules!', 'error');
        } else if ($('#enrichdb').val() === "integ") {
            loadEnrichTable(result);
        } else {
            $.getJSON(usr_dir + '/' + result, function (raw_data) {
                $('#dge').datagrid({
                    columns: [[
                            {field: 'pathname', title: 'Name', width: 160},
                            {field: 'hit', title: 'Hits', width: 40},
                            {field: 'pval', title: 'P-val', width: 60},
                            {field: 'padj', title: 'P-val(adj.)', width: 60},
                            {field: 'color', title: 'Color', width: 40}
                        ]]
                });
                currentEnrichFile = result.substring(0, result.length - 5);
                focus_enr_anot = raw_data['fun.anot'];
                var fun_hit = raw_data['hit.num'];
                var fun_pval = raw_data['fun.pval'];
                var fun_padj = raw_data['fun.padj'];
                var data_grid = $('#dge');
                //empty if there is any
                data_grid.datagrid('loadData', {
                    "total": 0,
                    "rows": []
                });
                var mdl_rows = [];
                var idx = 0;
                $.each(focus_enr_anot, function (k, v) {
                    mdl_rows.push({
                        pathname: k,
                        hit: fun_hit[idx],
                        pval: fun_pval[idx],
                        padj: fun_padj[idx]
                        , color: '<span id=\"function_' + idx + '\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>'
                    });
                    idx = idx + 1;
                });

                data_grid.datagrid({
                    onSelect: function (index, row) {
                        var nodeIDs = focus_enr_anot[row.pathname];
                        current_fun_nodes = nodeIDs;
                        highlightFunEnrichNodes(nodeIDs, row.pathname);
                        updateCellColor(highlightColor, "function_" + index);
                        if (current_main_plot === "score") {
                            addArrows(nodeIDs);
                        }
                    },
                    onUnselect: function (index, row) {
                        var nodeIDs = focus_enr_anot[row.pathname];
                        current_fun_nodes = null;
                        unHighlightFunEnrichNodes(nodeIDs);
                        updateCellColor("#ffffff", "function_" + index);
                        if (current_main_plot === "score") {
                            deleteArrows(nodeIDs);
                        }
                    }
                }).datagrid('loadData', mdl_rows);
            });
        }
        $.messager.progress('close');
    });
}

var currentEnrichFile = "";

function setEdgeColorAsNode() {
    if (gData.links.length === 0) {
        $.messager.alert('Error', 'No edges are detected within the scatter plot!', 'error');
        return;
    }
    if (current_main_plot === "score") {
        Scatter.graphData().links.forEach(function (l) {
            colorLineObj2(l, l.color)
        })
    } else {

    }
}

function getColorArr(clusters) {    
    var propNms = Object.keys(clusters);    
    $.ajax({
        beforeSend: function () {
            $.messager.progress({
                text: 'Processing .....'
            });
        },
        dataType: "html",
        type: "POST",
        url: '/MetaboAnalyst/faces/AjaxCall',
        data: {function: 'generateColorArray', number: propNms.length},
        async: false,
        success: function (result) {
            $.messager.progress('close');
            $.getJSON(usr_dir + "/" + result, function (jsonDat) {                
                var colArr = jsonDat;
                if (current_main_plot === "score") {
                    var nodes = Scatter.graphData().nodes;
                    setTimeout(function (n) {
                        for (var i = 0; i < nodes.length; i++) {
                            var n = nodes[i]
                            var inx = propNms.indexOf(n.cluster + "");
                            if (inx !== -1) {
                                n.colorb = colArr[inx];
                                n.tcolor = colArr[inx];
                                colorNodeObj(n, colArr[inx]);
                            } else {
                                sizeNodeObj(n, 0.01);
                            }
                        }
                    }, 1000);

                } else {
                    scene2.traverse(function (child) {
                        if (child instanceof THREE.Mesh) {
                            if (child.userData.nodeData !== undefined) {
                                var n = child.userData.nodeData;
                                var inx = propNms.indexOf(n.metatype);
                                if (inx !== -1) {
                                    colorNodeObj(n, colArr[inx]);
                                }
                            }
                        }
                    });
                }

                var mdl_rows = [];
                var idx = 0;

                for (var propNm in clusters) {
                    if (propNm !== "NA") {
                        mdl_rows.push({
                            index: idx,
                            origColor: colArr[idx],
                            ids: clusters[propNm],
                            name: "Cluster " + (idx + 1),
                            size: clusters[propNm].length,
                            selectionSettings: {
                                nodeSize: 2,
                                haloBool: false,
                                haloColor: "#ffffff",
                                boundary: "na",
                                boundaryColor: "#ffffff"
                            },
                            color: '<span id=\"meta_' + idx + '\" style="background-color:' + colArr[idx] + '" onclick="openModuleColorPicker(this);event.stopPropagation()">&nbsp;</span>',
                            colorhalo: '<button class="icon-edit" id=\"overall_' + idx + '\" style="width:16px;height:16px;border:0" onclick="setSelection(this);event.stopPropagation();event.stopPropagation()"></button>'
                        });
                        idx++;
                    }
                }
                $('#metadg').datagrid('loadData', []);
                $('#metadg').datagrid('loadData', mdl_rows);

                cluster_arr = mdl_rows;
                var sel = document.getElementById("targetSelection");
                removeOptions(sel);
                var arr = $("#metadg").datagrid("getRows")
                for (var i = 0; i < arr.length; i++) {
                    var option = document.createElement("option");
                    option.text = arr[i].name;
                    option.value = i;
                    sel.add(option);
                    count++
                }
                loadTargetedOpts();

            });
        }
    });
}

function sortObject(o) {
    var sorted = {},
            key, a = [];
    for (key in o) {
        if (o.hasOwnProperty(key)) {
            a.push(key);
        }
    }
    a.sort();
    for (key = 0; key < a.length; key++) {
        sorted[a[key]] = o[a[key]];
    }
    return sorted;
}

function performCustomClustering(selection) {

    var nodes_arr = [];
    var alg = $("#targetedOpts").val();
    if (alg === "NA") {
        return;
    }
    if (current_main_plot === "score") {
        var nodes = Scatter.graphData().nodes

        for (var i = 0; i < nodes.length; i++) {
            var n = nodes[i]
            if (n.cluster === selection || n.metatype === selection) {
                if (nodes_arr.length === 0) {
                    nodes_arr = [n.id];
                } else {
                    nodes_arr = nodes_arr + "; " + n.id;
                }
            }
        }

    } else {
        scene2.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (child.userData.nodeData !== undefined) {
                    var n = child.userData.nodeData;
                    if (n.cluster === selection || n.metatype === selection) {
                        if (nodes_arr.length === 0) {
                            nodes_arr = [n.id];
                        } else {
                            nodes_arr = nodes_arr + "; " + n.id;
                        }
                    }
                }
            }
        });
    }

    $.ajax({
        beforeSend: function () {
            $.messager.progress({
                text: 'Processing .....'
            });
        },
        dataType: "html",
        type: "POST",
        url: '/MetaboAnalyst/faces/AjaxCall',
        data: {function: 'performCustomClustering', IDs: nodes_arr, alg: alg},
        async: false,
        success: function (result) {
            $.messager.progress('close');
            $.getJSON(usr_dir + "/" + result, function (jsonDat) {
                var minclust = 1; //parseInt($('#minclust').val());
                var clusters = {};
                var nodes = []
                if (current_main_plot === "score") {
                    nodes = Scatter.graphData().nodes;
                } else {
                    nodes = []
                    scene2.traverse(function (child) {
                        if (child instanceof THREE.Mesh) {
                            if (child.userData.nodeData !== undefined) {
                                nodes.push(child.userData.nodeData)
                            }
                        }
                    });
                }

                nodes.forEach(function (n) {
                    n.targetedcluster = "NA";
                });

                for (var j = 0; j < jsonDat.cluster.inxs.length; j++) {
                    var n = nodes[jsonDat.cluster.inxs[j] - 1];
                    n.targetedcluster = jsonDat.cluster.cluster[j];
                    //n.metatype = jsonDat.cluster.cluster[j];
                    if (clusters[jsonDat.cluster.cluster[j]] === undefined) {
                        clusters[jsonDat.cluster.cluster[j]] = [n.id]
                    } else {
                        clusters[jsonDat.cluster.cluster[j]].push(n.id)
                    }

                }

                var mdl_rows = [];
                var idx = 0

                for (var propNm in clusters) {
                    mdl_rows.push({
                        index: idx,
                        group: selection,
                        ids: clusters[propNm],
                        name: "Cluster " + (idx + 1),
                        size: clusters[propNm].length,
                        selectionSettings: {
                            nodeSize: 2,
                            haloBool: false,
                            haloColor: "#ffffff",
                            boundary: "na",
                            boundaryColor: "#ffffff"
                        },
                        color: '<span id=\"cluster_' + idx + '\" style="background-color:#ffffff" onclick="openModuleColorPicker(this);event.stopPropagation()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>',
                        colorhalo: '<button class="icon-edit" id=\"targeted_' + idx + '\" style="width:16px;height:16px;border:0" onclick="setSelection(this);event.stopPropagation();"></button>'
                    });
                    idx++
                }

                $('#mdg').datagrid('loadData', []);
                $('#mdg').datagrid('loadData', mdl_rows);
                initMdgFunctions();
                cluster_arr2 = mdl_rows;
            });
        }
    });
}

function loadTargetedOpts() {
    //document.getElementById('selectTargetId').innerHTML = "<b>" + name + "</b>";

    var metaVal = $("#metaopt").val();
    console.log("loadTargetedOpts 1 --> " + metaVal);
    var sel = document.getElementById("targetedOpts");
    console.log("loadTargetedOpts 2 --> " + sel);
    removeOptions(sel);
    var algOpts = ["kmeans", "peak", "meanshift"]
    var algOptsText = ["K-means", "Density Peak", "Mean Shift"]
    if (algOpts.indexOf(metaVal) === -1) {
        for (var i = 0; i < algOpts.length; i++) {
            var option = document.createElement("option");
            option.text = algOptsText[i];
            option.value = algOpts[i];
            sel.add(option);
        }
    } else {
        var count = 0
        for (var propNm in gData.meta) {
            if (count === 0) {
                curr_meta = propNm
            }
            var option = document.createElement("option");
            option.text = propNm;
            sel.add(option);
            count++;
        }
    }
}

function performCustomMeta(meta) {
    var nodes = Scatter.graphData().nodes
    var targetedVal = $("#targetedOpts").val()
    for (var i = 0; i < nodes.length; i++) {
        var n = nodes[i]
        n.targetedcluster = gData.meta[targetedVal][i];
    }
    var clusters = {}
    for (var i = 0; i < nodes.length; i++) {
        var n = nodes[i]
        if (parseInt(n.cluster) === meta.index + 1) {
            if (clusters[n.targetedcluster] === undefined) {
                clusters[n.targetedcluster] = [n.id];
            } else {
                clusters[n.targetedcluster].push(n.id)
            }
        }
    }
    var mdl_rows = [];
    var idx = 0
    for (var propNm in clusters) {
        mdl_rows.push({
            group: targetedVal,
            index: idx,
            ids: clusters[propNm],
            name: propNm,
            size: clusters[propNm].length,
            color: '<span id=\"cluster_' + idx + '\" style="background-color:#ffffff" onclick="openModuleColorPicker(this);event.stopPropagation()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>',
            colorhalo: '<button class="icon-edit "id=\"targeted_' + idx + '\" style="width:16px;height:16px;border:0" onclick="setSelection(this);event.stopPropagation();"></button>'
        });
        idx++;
    }

    $('#mdg').datagrid('loadData', []);
    $('#mdg').datagrid('loadData', mdl_rows);
    initMdgFunctions();
    cluster_arr2 = mdl_rows;
}

function initMdgFunctions() {
    $('#mdg').datagrid({
        onSelect: function (index, row) {
            comp_last_selected_table = "targeted"
            updateCustomInfo(index, row);
            if (row_selection_on_user_click) {
                var ids_arr = row.ids
                Scatter.graphData().nodes.forEach(function (n) {
                    if (ids_arr.indexOf(n.id) !== -1) {
                        colorNodeObj(n, highlightColor)
                    }
                });
                updateCellColor(highlightColor, "cluster_" + index);
            }
            if (row.name.includes("Cluster")) {
                displayPatternData(row, "targetedcluster");
            } else {
                displayPatternData(row, "meta");
            }
        },
        onUnselect: function (index, row) {
            //current_selected_row = "";
            current_fun_nodes = null;
            var e = cluster_arr2[index];
            if (e !== undefined) {
                cluster_arr2[index].focused = true
                selectClusterNew(e)
            }
            updateCellColor("#ffffff", "cluster_" + index);
        }
    })

}

function setSelection(element) {
    current_module = element.id;
    var curType = current_module.split(/_(.+)/)[0];
    curType = curType.replace("overall", "meta");
    curType = curType.replace("targeted", "cluster");
    var curIndex = current_module.split(/_(.+)/)[1];

    var row;
    if (curType === "meta") {
        row = $("#metadg").datagrid("getRows")[curIndex];
        $('#metadg').datagrid('selectRow', curIndex);
        //updateMetaInfo(curIndex, row)
    } else {
        row = $("#mdg").datagrid("getRows")[curIndex]
        $('#mdg').datagrid('selectRow', curIndex);
        //updateCustomInfo(curIndex, row)
    }
    $("#nodeSliderSize").val(parseInt(row.selectionSettings.nodeSize));
    $("#nsize").val(parseInt(row.selectionSettings.nodeSize));
    document.getElementById("haloCheckbox").checked = row.selectionSettings.haloBool;
    $("#customHalo").spectrum("set", row.selectionSettings.haloColor);
    $("#encasingType").val(row.selectionSettings.boundary);
    //$("#customEncasing").spectrum("set", row.selectionSettings.boundaryColor);
    if ($('#viewFocusOpt').val() !== "score") {
        $.messager.alert('Error', 'Please first update the current View Type to Scores!', 'error');
        return;
    } else {
        $('#highlightdlg').dialog('open');
    }
}

function highlightNodesAndHideAllOther(ids) {
    var nodes;
    if (current_main_plot === "score") {
        nodes = Scatter.graphData().nodes
        nodes.forEach(function (n) {
            if (ids.indexOf(n.id) === -1) {
                opaNodeObj(n, 0.2)
            } else {
                opaNodeObj(n, 0.8)
            }
        });
    } else {
        scene2.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (ids.indexOf(child.userData.nodeData.id) === -1) {
                    child.material.transparent = false;
                    child.material.opacity = 0.2;
                } else {
                    child.material.transparent = false;
                    child.material.opacity = 0.8;
                }
            }
        });
    }
}

function deleteSpherical(sceneType) {
    if (sceneType === "main") {
        var scene = Scatter.scene();
        var sphereMesh = sphereMesh1
    } else {
        var scene = scene2;
        var sphereMesh = sphereMesh2
    }

    if (sphereMesh !== undefined) {
        scene.remove(sphereMesh);
        if (sphereMesh.geometry !== undefined) {
            sphereMesh.geometry.dispose();
            sphereMesh.geometry = undefined;
            if (sphereMesh.material.map)
                sphereMesh.material.map.dispose();
            if (sphereMesh.material.lightMap)
                sphereMesh.material.lightMap.dispose();
            if (sphereMesh.material.bumpMap)
                sphereMesh.material.bumpMap.dispose();
            if (sphereMesh.material.normalMap)
                sphereMesh.material.normalMap.dispose();
            if (sphereMesh.material.specularMap)
                sphereMesh.material.specularMap.dispose();
            if (sphereMesh.material.envMap)
                sphereMesh.material.envMap.dispose();
            sphereMesh.material.dispose();
            sphereMesh.material = undefined;
            sphereMesh = undefined;
        }
    }
    //Graph.scene().add(edgeMesh);
}

function calculateRadius(sceneNum, omics, number) {
    var radiusId;
    var xVal;
    var yVal;
    var selId = [];
    if (omics === "both") {
        for (var propNm in gData.sigMat) {
            for (var i = 0; i < gData.sigMat[propNm].length; i++) {
                if (i === number) {
                    selId.push(gData.sigMat[propNm][i].ids);
                }
            }
        }
    } else {
        for (var i = 0; i < gData.sigMat[omics].length; i++) {
            if (i === number) {
                selId.push(gData.sigMat[omics][i].ids);
            }
        }
    }

    if (sceneNum === 1) {
        var nodes = Scatter.graphData().nodes
        nodes.forEach(function (n) {
            if (selId.indexOf(n.id) !== -1) {

            }
        });
    } else if (sceneNum === 2) {
        scene2.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (ids.indexOf(child.userData.nodeData.id) === -1) {
                    child.material.transparent = false;
                    child.material.opacity = 0.2;
                } else {
                    child.material.transparent = false;
                    child.material.opacity = 0.8;
                }
            }
        });
    }
}

function distanceVector(v1, v2)
{
    var dx = v1.x - v2.x;
    var dy = v1.y - v2.y;
    var dz = v1.z - v2.z;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
}

function colorLinkByNode() {
    Scatter.graphData().links.forEach(function (l) {
        colorLineObj2(l, l.color);
    });
}

function updateTextInputSph(val) {
    document.getElementById('textSphereOpacity').value = val;
}

function updateTextInputSphSize(val) {
    document.getElementById('textSphereSize').value = val;
}

function changeSphere() {
    var opacity = parseFloat($('#sphereOpacity').val());
    sphereOpacity = opacity;
    var size = parseFloat($('#sphereSize').val());
    changeColorSpherical(sphereColor, sphereOpacity, size);

    //$('#optdlg').dialog('close');
}

function changeColorSpherical(colorr, opacity, size) {
    var sphereMesh;
    var color = new THREE.Color(colorr);
    if (current_main_plot === "score") {
        sphereMesh = sphereMesh2
    } else {
        sphereMesh = sphereMesh1
    }
    if (size === 0) {
        sphereMesh.visible = false;
        return;
    } else {
        sphereMesh.visible = true;

    }

    var scale = (size * 1000) / 500

    var sphere = new THREE.Sphere();
    sphere.radius = scale * 500

    if (current_main_plot === "score") {
        scene2.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (child.userData.nodeData !== undefined) {
                    var n = child.userData.nodeData;


                    var point = new THREE.Vector3(n.fx, n.fy, n.fz)
                    if (sphere.containsPoint(point)) {
                        var colorValue = parseInt("0xd6d6d6", 16);
                        var col = new THREE.Color(colorValue);
                        child.material.color = col;
                        child.material.opacity = 0.2;
                    } else {
                        var colorValue = parseInt(n.colorb.replace("#", "0x"), 16);
                        var col = new THREE.Color(colorValue);
                        child.material.color = col;
                        child.material.opacity = 0.8;
                    }
                }
            }
        });
    } else {
        var nodes = Scatter.graphData().nodes;
        nodes.forEach(function (n) {
            var point = new THREE.Vector3(n.fx, n.fy, n.fz)
            if (sphere.containsPoint(point)) {

                colorNodeObj(n, "#d6d6d6");
                opaNodeObj(n, 0.2);

            } else {
                colorNodeObj(n, n.tcolor);
                opaNodeObj(n, 0.8);

            }
        });
    }

    sphereMesh.scale.x = scale;
    sphereMesh.scale.y = scale;
    sphereMesh.scale.z = scale;
    sphereMesh.material.color = color;
    sphereMesh.material.opacity = opacity;
}

function switchToLoading() {
    $("#loader").show();
    if (current_main_plot === "loading") {
        return;
    }
   
    current_main_plot = "loading";

    var nav = gData.navigation;
    var main = gData.nodes
    gData.navigation = main;
    gData.nodes = nav;
    gData.edgesMain = gData.links
    gData.links = [];
    initScatter2();
    for (propNm in contours_info_obj) {
        var cont = contours_info_obj[propNm]
        if (cont.type === "contour") {
            //computeContours(scene2, cont.ids, cont.color)
        } else {
            //computeEllipsoid(scene2, cont.ids, cont.color, cont.type)
        }
    }
    //computeContours(scene2)
    Scatter.graphData(gData)
    deleteSpherical("inset")
    setTimeout(function () {
        Scatter.graphData().nodes.forEach(function (nd) {
            nd.highlight = 0;
            colorNodeObj(nd, nd.tcolor)
            opaNodeObj(nd, 1)
            deleteOutline(nd)
        });
        addLoadingSphere("main", 500, sphereColor, sphereOpacity)

        var val = $("#loadingOpt").val();
        if (val !== "both" && !val.includes("pca_")) {
            var nodes = Scatter.graphData().nodes;
            nodes.forEach(function (n) {
                if (!n.omicstype.includes(val)) {
                    n.__threeObj.visible = false;
                } else {
                    n.__threeObj.visible = true;

                }
            });
        }
    }, 10);
    setTimeout(function () {

        $("#loader").hide();
    }, 1000);
}

function switchToScore() {
    $("#loader").show();
    //$("#loadingOpt").attr("disabled", false);
    if (current_main_plot === "score") {
        return;
    }
    current_main_plot = "score"
    //deleteClusteringShapes(scene2)
    var nav = gData.navigation;
    var main = gData.nodes
    gData.navigation = main;
    gData.nodes = nav;
    var loadval = $("#loadingOpt").val();
    if (!loadval.includes("pca_")) {
        if (gData.edgesMain !== undefined) {
            gData.links = gData.edgesMain
        } else {
            gData.links = gData.edges
        }
    }
    gData.edgesMain = null;
    initScatter2();
    //computeContours(Scatter.scene())
    for (propNm in contours_info_obj) {
        var cont = contours_info_obj[propNm]
        if (cont.type === "contour") {
            //computeContours(Scatter.scene(), cont.ids, cont.color)
        } else {
            //computeEllipsoid(Scatter.scene(), cont.ids, cont.color, cont.type)
        }
    }
    deleteSpherical("main")
    //console.log(gData)
    Scatter.graphData(gData).linkOpacity(0)
    setTimeout(function () {
        Scatter.graphData().nodes.forEach(function (nd) {
            nd.highlight = 0
            colorNodeObj(nd, nd.tcolor)
            opaNodeObj(nd, 1)
            sizeNodeObj(nd, 1)
            deleteOutline(nd)
        });
        addLoadingSphere("inset", 500, sphereColor, sphereOpacity)

        var val = $("#loadingOpt").val();
        if (val !== "both" && !val.includes("pca_")) {
            scene2.traverse(function (child) {
                if (child instanceof THREE.Mesh) {
                    var n = child.userData.nodeData;
                    if (n !== undefined) {

                        if (!n.omicstype.includes(val)) {
                            child.visible = false;
                        } else {
                            child.visible = true;

                        }
                    }
                }
            });
        }
    }, 10);
    setTimeout(function () {

        $("#loader").hide();
    }, 1000);


}

function addLoadingSphere(scene, radius, color, opacity) {
    //deleteSpherical(scene)
    sphericalRadius = radius;
    var sphereGeometry = new THREE.SphereGeometry(sphericalRadius, 64, 64);
    var sphereMaterial = new THREE.MeshPhongMaterial({
        opacity: opacity,
        color: color,
        transparent: true
    });

    if (scene === "main") {
        sphereMesh1 = new THREE.Mesh(sphereGeometry, sphereMaterial);
        sphere1 = new THREE.Sphere();
        sphere1.radius = sphericalRadius
        Scatter.scene().add(sphereMesh1);

        var nodes = Scatter.graphData().nodes;
        nodes.forEach(function (n) {
            var point = new THREE.Vector3(n.fx, n.fy, n.fz)
            if (sphere1.containsPoint(point)) {

                colorNodeObj(n, "#d6d6d6");
                opaNodeObj(n, 0.2)
            }
        });

    } else {
        sphereMesh2 = new THREE.Mesh(sphereGeometry, sphereMaterial);
        sphere2 = new THREE.Sphere();
        sphere2.radius = sphericalRadius
        scene2.add(sphereMesh2);

        scene2.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (child.userData.nodeData !== undefined) {
                    var n = child.userData.nodeData;
                    var point = new THREE.Vector3(n.fx, n.fy, n.fz)
                    if (sphere2.containsPoint(point)) {
                        var colorValue = parseInt("0xd6d6d6", 16);
                        var col = new THREE.Color(colorValue);
                        child.material.color = col;
                        child.material.opacity = 0.2;
                    }
                }
            }
        });
    }

}

function toggleOmicsType() {
    console.log("run --> toggleOmicsType");
    var val = $("#loadingOpt").val();
    var inx = gData.omicstype.indexOf(val);
    currentOmicsType = val;
    if (isReductOptSingle || val.includes("pca_")) {
        $("#omicstype").attr("disabled", true);
        var omicstype = val.replace("pca_", "");
        $("#omicstype").val(omicstype)
        $("#biplotOmicsOpt").val(omicstype)
    } else {
        $("#omicstype").attr("disabled", false);
        $("#biplotOmicsOpt").val(omicstype)
    }
    var type = $("#axisOpt").val();

    for (var propNm in labelArr) {
        var text = labelArr[propNm].userData.id

        var keyNames = Object.keys(myTexts)
        if (keyNames.indexOf(text + "" + scene2.uuid) !== -1) {
            myTexts[text + "" + scene2.uuid].visible = false
            delete myTexts[text + "" + scene2.uuid]
            scene2.remove(myTexts[text + "" + scene2.uuid])
        }
        if (keyNames.indexOf(text + "" + Scatter.scene().uuid) !== -1) {
            myTexts[text + "" + Scatter.scene().uuid].visible = false
            delete myTexts[text + "" + Scatter.scene().uuid]
            Scatter.scene().remove(myTexts[text + "" + Scatter.scene().uuid])
        }

    }

    generateAxisLabel(type, Scatter.scene());
    generateAxisLabel(type, scene2);
    if ((val === "both" || !val.includes("pca_")) && !isReductOptSingle) {

        if (current_main_plot === "score") {
            var sel = gData[["both"]]
            gData.nodes = sel;
            if (gData.edges !== undefined) {
                gData.links = gData.edges;
            }
            Scatter.graphData(gData)

            var sel = gData["loading"];
            gData.navigation = sel;
            if (gData.reductionOpt !== "mcia") {
                gData.links = [];
            }
            initScatter2();
        } else {

            var score = gData[["loading"]]
            var count = 0
            gData.nodes = score;
            Scatter.graphData(gData)


            var loading = gData[["both"]]

            if (loading !== undefined) {
                gData.navigation = loading;
                if (gData.reductionOpt !== "mcia") {
                    gData.links = [];
                } else {
                    gData.mainEdges = gData.mainLinks;
                }
                initScatter2();
            }
        }

        if (current_main_plot === "score") {
            scene2.traverse(function (child) {
                if (child instanceof THREE.Mesh) {
                    var n = child.userData.nodeData;
                    if (n !== undefined) {
                        child.visible = true;
                    }
                }
            });
        } else {
            var nodes = Scatter.graphData().nodes;
            nodes.forEach(function (n) {
                n.__threeObj.visible = true;
            });

        }
    } else {
        var nodeData;
        if (isReductOptSingle || val.includes("pca_")) {
            if (val.includes("pca_")) {
                if (current_main_plot === "score") {
                    var sel = gData[[val]]
                    gData.nodes = sel;
                    gData.links = [];
                    Scatter.graphData(gData)

                    var sel = gData[val + "_loading"];
                    gData.navigation = sel;
                    gData.links = [];
                    initScatter2();
                } else {

                    var sel = gData[[val + "_loading"]]
                    var count = 0
                    gData.nodes = sel;
                    gData.links = [];
                    Scatter.graphData(gData)

                    var sel = gData[[val]]
                    gData.navigation = sel;
                    gData.links = [];
                    initScatter2();
                }
            } else if (isReductOptSingle) {
                if (current_main_plot === "score") {

                    var sel = gData[val];
                    gData.nodes = sel;
                    gData.links = [];
                    Scatter.graphData(gData)

                    var sel = gData[["loading"]]
                    gData.navigation = sel;
                    gData.links = [];
                    initScatter2();
                } else {
                    var sel = gData["loading"];
                    gData.nodes = sel;
                    gData.links = [];
                    Scatter.graphData(gData)

                    var sel = gData[val];
                    gData.navigation = sel;
                    gData.links = [];
                    initScatter2();
                }
            }
        }
    }

    setTimeout(function () {
        Scatter.graphData().nodes.forEach(function (nd) {
            nd.highlight = 0
            colorNodeObj(nd, nd.tcolor)
            opaNodeObj(nd, 1)
            sizeNodeObj(nd, 1)
            deleteOutline(nd)
        });
    }, 10);
    //console.log(val)
    if (val !== "both") {
        if (current_main_plot === "score") {
            var type = val.replace("pca_", "");
            scene2.traverse(function (child) {
                if (child instanceof THREE.Mesh) {
                    var n = child.userData.nodeData;
                    //console.log(n.omicstype)
                    if (n !== undefined) {
                        if (!n.omicstype.includes(type)) {
                            child.visible = false;
                        } else {
                            child.visible = true;

                        }
                    }
                }
            });
        } else {
            var nodes = Scatter.graphData().nodes;
            nodes.forEach(function (n) {
                if (n.__threeObj !== undefined) {
                    if (!n.omicstype.includes(type)) {
                        n.__threeObj.visible = false;
                    } else {
                        n.__threeObj.visible = true;

                    }
                }
            });

        }
    }
    //if (["mcia"].indexOf(gData.reductionOpt) !== -1) {
    loadMeta();
    //}
    greyOutNodes();

}

function greyOutNodes() {

    if (current_main_plot === "score") {
        scene2.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
                if (child.userData.nodeData !== undefined) {
                    var n = child.userData.nodeData;
                    var point = new THREE.Vector3(n.fx, n.fy, n.fz)
                    if (sphere2.containsPoint(point)) {
                        var colorValue = parseInt("0xd6d6d6", 16);
                        var col = new THREE.Color(colorValue);
                        child.material.color = col;
                        child.material.opacity = 0.2;
                    }
                }
            }
        });

    } else {

        var nodes = Scatter.graphData().nodes;
        nodes.forEach(function (n) {
            var point = new THREE.Vector3(n.fx, n.fy, n.fz)
            if (sphere1.containsPoint(point)) {

                colorNodeObj(n, "#d6d6d6");
                opaNodeObj(n, 0.2);
            }
        });

    }
}

function doEncasing(type) {
    console.log("run --> doEncasing");
    if (type === "biplot") {
        var meta = $('#biplotMetaOpt').val();
        var encasingType = $('#biplotEncasingType').val();
        var confVal = $('#textInputBiplotConf').val();
        var alphaVal = $('#textInputBiplotAlpha').val();
    } else {
        var meta = $('#encasingMetaOpt').val();
        var encasingType = $('#scoreEncasingType').val();
        var confVal = $('#textInputEncasingConf').val();
        var alphaVal = $('#textInputEncasingAlpha').val();
    }

    //var alphaVal = 0.7
    var selIds = [];

    var length;
    var dir = new THREE.Vector3(1, 0, 0);
    var origin = new THREE.Vector3(0, 0, 0);
    var colorValue = parseInt(arrowColorU.replace("#", "0x"), 16);
    var colored = new THREE.Color(colorValue);


    var nodes_vec = [];
    var xArr = [];
    var yArr = [];
    var zArr = [];
    var meansArr = [];
    curr_meta = meta;
    loadMeta();
    var arr = gData.meta[curr_meta];
    var idsObj = {};
    var set = [...new Set(arr)]
    var colorArr = []
    setTimeout(function () {
        for (var i = 0; i < set.length; i++) {
            var idsArr = [];
            var col = {};
            Scatter.graphData().nodes.forEach(function (n) {
                if (n.metatype === set[i]) {
                    nodes_vec.push(n.id)
                    xArr.push(n.fx)
                    yArr.push(n.fy)
                    zArr.push(n.fz)
                    idsArr.push(n.id);
                    col[set[i]] = n.color;
                }
            });

            var meanX = meanF(xArr)
            var meanY = meanF(yArr)
            var meanZ = meanF(zArr)
            var meanXYZ = v(meanX, meanY, meanZ)
            meansArr.push(meanXYZ);

            (function (i) {
                computeEncasing(idsArr, encasingType, col[set[i]], set[i], confVal, alphaVal)
                //rest of the code
            }(i));

        }
        deleteSpherical("main");
    }, 1);

    /*
     for (var i = 0; i < set.length; i++) {
     dir = meansArr[i];
     console.log(set[i])
     var length = distanceVector(origin, dir)
     var arrow = new THREE.ArrowHelper(dir.normalize(), origin, length, colored, 0.1 * length, 0.4 * 0.1 * length);
     var text = createText2D(set[i], Scatter.scene().uuid);
     biplotArrows[set[i]] = arrow;
     biplotLabels[set[i]] = text;
     Scatter.scene().add(arrow)
     Scatter.scene().add(text)
     text.position.set(dir.x, dir.y, dir.z);
     
     }
     */
    //$.messager.progress('close');
    //$("#biplotdlg2").dialog("close");
}

function globalEncasing(callback) {
    if (current_main_plot !== "score") {
        switchToScoreCallBack(function () {
            doEncasing("encasing");
        });
    } else {
        doEncasing("encasing");
    }
    callback();
    //$("#encasingdlg").dialog("close");
}

function switchToLoadingCallBack(callBack) {
    //$("#loadingOpt").attr("disabled", true);
    current_main_plot = "loading"
    //deleteClusteringShapes(Scatter.scene())
    var nav = gData.navigation;
    var main = gData.nodes
    gData.navigation = main;
    gData.nodes = nav;
    gData.edgesMain = gData.links
    gData.links = [];
    initScatter2();
    for (propNm in contours_info_obj) {
        var cont = contours_info_obj[propNm]
        if (cont.type === "contour") {
            //computeContours(scene2, cont.ids, cont.color)
        } else {
            //computeEllipsoid(scene2, cont.ids, cont.color, cont.type)
        }
    }
    //computeContours(scene2)
    Scatter.graphData(gData)
    deleteSpherical("inset")
    setTimeout(function () {
        Scatter.graphData().nodes.forEach(function (nd) {
            nd.highlight = 0;
            colorNodeObj(nd, nd.tcolor)
            opaNodeObj(nd, 1)
            deleteOutline(nd)
        });
        addLoadingSphere("main", 500, sphereColor, sphereOpacity)

        var val = $("#loadingOpt").val();
        if (val !== "both") {
            var nodes = Scatter.graphData().nodes;
            nodes.forEach(function (n) {
                if (!n.omicstype.includes(val)) {
                    n.__threeObj.visible = false;
                } else {
                    n.__threeObj.visible = true;

                }
            });
        }
        return callBack();
    }, 10);
}

function switchToScoreCallBack(callBack) {
    //$("#loadingOpt").attr("disabled", false);
    current_main_plot = "score"
    //deleteClusteringShapes(scene2)
    var nav = gData.navigation;
    var main = gData.nodes
    gData.navigation = main;
    gData.nodes = nav;
    if (gData.edgesMain !== undefined) {
        gData.links = gData.edgesMain
    } else {
        gData.links = gData.edges
    }
    gData.edgesMain = null;
    initScatter2();
    //computeContours(Scatter.scene())
    for (propNm in contours_info_obj) {
        var cont = contours_info_obj[propNm]
        if (cont.type === "contour") {
            //computeContours(Scatter.scene(), cont.ids, cont.color)
        } else {
            //computeEllipsoid(Scatter.scene(), cont.ids, cont.color, cont.type)
        }
    }
    deleteSpherical("main")
    Scatter.graphData(gData)
    setTimeout(function () {
        Scatter.graphData().nodes.forEach(function (nd) {
            nd.highlight = 0
            colorNodeObj(nd, nd.tcolor)
            opaNodeObj(nd, 1)
            sizeNodeObj(nd, 1)
            deleteOutline(nd)
        });
        addLoadingSphere("inset", 500, sphereColor, sphereOpacity)

        var val = $("#loadingOpt").val();
        if (val !== "both") {
            scene2.traverse(function (child) {
                if (child instanceof THREE.Mesh) {
                    var n = child.userData.nodeData;
                    if (n !== undefined) {

                        if (!n.omicstype.includes(val)) {
                            child.visible = false;
                        } else {
                            child.visible = true;

                        }
                    }
                }
            });
        }
        return callBack();
    }, 10);

}

function setGradientColor(col1, col2) {
    var canvas = document.createElement("canvas");
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    var ctx = canvas.getContext("2d");
    var grd = ctx.createLinearGradient(0, 0, 0, window.innerHeight);
    grd.addColorStop(0, col1);
    grd.addColorStop(1, col2);

    ctx.fillStyle = grd;
    ctx.fillRect(0, 0, window.innerWidth, window.innerHeight);

    var texture = new THREE.CanvasTexture(canvas);

// main part
    if (mainCheckBool) {
        Scatter.scene().background = texture;
    } else {
        scene2.background = texture;
    }

}

function addArrows(selIds) {
    var nms = Object.keys(biplotArrows)
    if (selIds.length === 1) {
        if (nms.indexOf(selIds[0]) !== -1) {
            return;
        }
    } else {

    }
    var textBool = $('#biplotTextDisplayOpt').val();
    var dir = new THREE.Vector3(1, 0, 0);
    var origin = new THREE.Vector3(0, 0, 0);
    var colorValue = parseInt(highlightColor.replace("#", "0x"), 16);
    var colored = new THREE.Color(colorValue);
    var count = 0;
    scene2.traverse(function (child) {
        if (child instanceof THREE.Mesh) {
            if (child.userData.nodeData !== undefined) {
                if (selIds.indexOf(child.userData.nodeData.id) !== -1 || selIds.indexOf(child.userData.nodeData.label) !== -1) {

                    var n = child.userData.nodeData;
                    dir = new THREE.Vector3(n.fx, n.fy, n.fz);
                    var length = distanceVector(origin, dir)
                    var arrow = new THREE.ArrowHelper(dir.normalize(), origin, length, colored, 0.1 * length, 0.4 * 0.1 * length);
                    if (biplotArrows[n.id] !== undefined) {
                        deleteMesh(biplotLabels[n.id], Scatter.scene());
                        delete biplotLabels[n.id];
                        deleteMesh(biplotArrows[n.id], Scatter.scene());
                        delete biplotArrows[n.id];
                    }
                    biplotArrows[n.id] = arrow;
                    Scatter.scene().add(arrow)

                    if (textBool !== "none") {
                        var text = createText2D(n.label, Scatter.scene().uuid);
                        biplotLabels[n.id] = text;
                        Scatter.scene().add(text)
                        text.position.set(n.fx, n.fy, n.fz);

                    }

                    count++;
                }
            }
        }
    });
}

function deleteArrows(selIds) {
    if (current_main_plot === "score") {
        var nms = Object.keys(biplotArrows)
        for (var i = 0; i < nms.length; i++) {
            var nm = nms[i]
            if (selIds.indexOf(nm) !== -1) {
                deleteMesh(biplotArrows[nm], Scatter.scene());
                delete biplotArrows[nm];
            }
        }
        nms = Object.keys(biplotLabels)
        for (var i = 0; i < nms.length; i++) {
            var nm = nms[i]
            if (selIds.indexOf(nm) !== -1) {
                deleteMesh(biplotLabels[nm], Scatter.scene());
                delete biplotLabels[nm];
            }
        }
    }
}

function setDualView() {
    embeddedCollapsed = false;
    $('#loader').show();
    toggleOmicsType();
    $('#loader').hide();
}

(function ($) {
    $.extend($.fn.window.methods, {
        moveTo: function (jq, param) {
            return jq.each(function () {
                var win = $(this).window('window');
                var width = win.outerWidth();
                var height = win.outerHeight();
                var left = undefined;
                var top = undefined;
                if (param.indexOf('left') >= 0) {
                    left = $(document).scrollLeft();
                }
                if (param.indexOf('right') >= 0) {
                    left = $(window).outerWidth() - width + $(document).scrollLeft() - 20;
                }
                if (param.indexOf('top') >= 0) {
                    top = $(document).scrollTop();
                }
                if (param.indexOf('bottom') >= 0) {
                    top = $(window).outerHeight() - height + $(document).scrollTop();
                }
                $(this).window('move', {
                    left: left,
                    top: top
                });
            })
        }
    });
})(jQuery);

function switchToScoreFromDialog() {
    //var encasingBool = document.getElementById("yes").checked;
    //if (encasingBool) {
    $("#loader").show()
    globalEncasing(function () {

        //$("#loader").hide()
    });





    //} else {
    //    switchToScore();
    //    $('#encasingdlg').dialog('close');
    //}
}

function handleRadio(myRadio) {
    var val = myRadio.value;
    if (val === "no") {
        document.getElementById("myTableFieldSet").disabled = true;
    } else {
        document.getElementById("myTableFieldSet").disabled = false;
    }

}

function removeAllOverlays() {
    var nms = Object.keys(boundingClusters);
    for (var i = 0; i < nms.length; i++) {
        var nm = nms[i]
        deleteMetaSphere(nm, Scatter.scene());
    }
}

//Author: Zhiqiang Pang
function clearSelection() {
    console.log("Running into clearSelection");
    Scatter.graphData().nodes.forEach(function (n) {
        if (n.highlight === 1) {
            n.color = n.tcolor;
            unhighlightNode(n);
            highlightNode(n);
        }
    });
}