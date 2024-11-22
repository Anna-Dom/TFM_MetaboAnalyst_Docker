/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function initUpsetPlot() {
    var res = $.ajax({
        type: "GET",
        url: '/MetaboAnalyst/faces/AjaxCall?function=getUpsetInfo&ignoreMe=' + new Date().getTime(),
        async: false
    }).responseText;
    var jobinfo = res.split("||");
    usr_dir = jobinfo[0];
    jsonNm = jobinfo[1] + ".json";
    $.getJSON(usr_dir + jsonNm, function (res) {
        var mode = $('#modeOpt').val()
        var result = res[0];
        var color_arr = res[1]
        var newArr = [];
        for (var i = 0; i < result.length; i++) {
            if (result[i].sets.length !== 0) {
                var obj = {}
                obj.name = result[i].name;
                obj.sets = result[i].sets;
                if (typeof obj.sets === "string") {
                    obj.sets = [obj.sets];
                }
                newArr.push(obj)
            }
        }

        const elems = newArr;
        console.log(UpSetJS)
        var {sets, combinations} = UpSetJS.extractCombinations(elems,
                {
                    "type": mode
                });
        let selection = null;

        sets = sets.sort(compare);

        var i = sets.length
        while (i--) {

            sets[i].color = color_arr[i]
        }

        var i = combinations.length
        combinations = combinations.sort(compare2);
        while (i--) {
            var colorArr = []
            combinations[i].sets.forEach((elem) => {
                colorArr.push(elem.color)
            })
            combinations[i].color = UpSetJS.mergeColors(colorArr);
        }


        function onHover(set) {
            selection = set;
            render();
        }

        function onClick(set) {

            $("#displayed-data").remove();

            $("#selected-data").append('<span id="displayed-data" class="displayed-data"></span>');
            if (set === null) {
                queries = [];
                selection = [];
                render();
                return;
            }
            if (set.elems.length > 0) {
                var t = "<ul><lh>Total:" + set.elems.length + "</lh>";
                for (var i = 0; i < set.elems.length; i++) {
                    t = t + '<li>' + set.elems[i].name + '</li>';
                }
                t = t + '</ul>';
                $("#displayed-data").append(t);
            }
            queries = [
                set
            ];
            render();

        }

        var queries = [

        ];

        var width;
        var height;
        if (sets.length === 2) {
            width = 600;
            height = 600;
        } else if (sets.length === 3) {
            width = 800;
            height = 600;
        } else {
            width = 1000;
            height = 750;
        }

        obj = {
            "widthRatios": [
                0.135,
                0.165
            ],
            "fontSizes": {
                "setLabel": "5px"
            }
        }
        //console.log(sets.length)
        function render() {
            document.getElementById("venn-demo").style.width = width + "px";
            document.getElementById("venn-demo").style.height = height + "px";
            //const props = {sets, combinations, width: width, height: height, selection, onHover, onClick, queries, obj};
            var props = Object.assign({
                width: width,
                height: height,
            }, {
                sets: sets,
                combinations: combinations,
                selection: selection,
                queries: queries,
                onHover: onHover,
                onClick: onClick
            }, {
                "widthRatios": [
                    0.21,
                    0.14
                ],
                "fontSizes": {
                    "setLabel": "13px",
                    "chartLabel": "13px"
                },
                "exportButtons": false
            });
            if (sets.length > 3) {
                props["alternatingBackgroundColor"] = "#d3d3d3";
            }
            UpSetJS.render(document.getElementById("venn-demo"), props);
            //hiding dataset size axis
            //var setaxis = document.querySelectorAll('[data-upset="setaxis"]')[0]
            //setaxis.style.display = "none";
        }

        render();
    });
}

function exportImage(type) {
    var svg = document.getElementsByTagName("svg")[0]; //can only have one svg in the doc

    UpSetJS.exportSVG(svg, {
        title: "UpSet_plot",
        type: type
    })

}
function mergeColors(colors) {
    if (colors.length === 0) {
        return undefined;
    }
    if (colors.length === 1) {
        return colors[0];
    }
    const cc = colors.reduce(
            (acc, d) => {
        const c = lab(d || 'transparent');
        return {
            l: acc.l + c.l,
            a: acc.a + c.a,
            b: acc.b + c.b,
        };
    },
            {l: 0, a: 0, b: 0}
    );
    return lab(cc.l / colors.length, cc.a / colors.length, cc.b / colors.length).toString();
    // return null;
}

function compare(a, b) {
    if (a["name"] < b["name"]) {
        return -1;
    }
    if (a["name"] > b["name"]) {
        return 1;
    }
    return 0;
}

function compare2(a, b) {
    if (a["cardinality"] < b["cardinality"]) {
        return 1;
    }
    if (a["cardinality"] > b["cardinality"]) {
        return -1;
    }
    return 0;
}

