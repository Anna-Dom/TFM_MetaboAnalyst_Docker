var fcDt = {};
var colors_gbr20_arr = ["#00E700", "#00CE00", "#00B400", "#009B00", "#008100", "#006700", "#004E00", "#003400", "#001A00", "#000100", "#010000", "#1A0000", "#340000", "#4E0000", "#670000", "#810000", "#9B0000", "#B40000", "#CE0000", "#E70000"];
var initVar = true;
var defaultEdgeSize = 0.4;
var backgroundColor = "white";
var cx1;
var raw_data;
var myChart;

function showMummichog() {
    $.getJSON('/MetaboAnalyst' + document.getElementById("mydir").value + '/scattermum.json',
            function (data) {
                //document.getElementById("spinner").style.display = "block";
                showScatter2DMum(data);
            });
}

function showGSEA() {
    $.getJSON('/MetaboAnalyst' + document.getElementById("mydir").value + '/scattergsea.json',
            function (data) {
                //document.getElementById("spinner").style.display = "block";
                showScatter2DGSEA(data);
            });
}

function showInteg() {
    $.getJSON('/MetaboAnalyst' + document.getElementById("mydir").value + '/scatterinteg.json',
            function (data) {
                //document.getElementById("spinner").style.display = "block";
                showScatter2DInteg(data);
            });
}

function rescale2Range(inputY, yMax, yMin, xMax, xMin) {
    var percent = (inputY - yMin) / (yMax - yMin);
    var outputX = percent * (xMax - xMin) + xMin;
    return outputX;
}


function exportToPng() {

    var link = document.createElement('a');
    link.download = 'scatter_plot.png';
    link.href = myChart.toBase64Image();
    link.click();
}

function closest(arr, closestTo) {
    var closest = Math.max.apply(null, arr); //Get the highest number in arr in case it match nothing.
    for (var i = 0; i < arr.length; i++) { //Loop the array
        if (arr[i] >= closestTo && arr[i] < closest)
            closest = arr[i]; //Check if it's higher than your number, but lower than your closest value
    }
    return closest; // return the value
}

function reset() {
    myChart.resetZoom();
}

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var gridSVGCoords = "";
var gridSVGMappings = "";
// select elements

function showScatter2DMum(dat) {

    var colArr = [];

    var arrlength = dat['enr'].length;
    var rainbow = generateColorGradient(["#ff0000", "#FFEF00", "#ffffff"], arrlength, 0, arrlength);
    for (var i = 0; i < arrlength; i++) {
        colArr.push('#' + rainbow.colourAt(i));
    }

    var sizeArr = generateSizeGradient(dat["pval"], 5, 20);
    var hoverSizeArr = generateSizeGradient(dat["pval"], 6, 24);

    //console.log(sizeArr);
    var dataset = [];
    for (var i = 0; i < dat["enr"].length; i++) {
        var arr = {
            x: dat["enr"][i],
            y: dat["pval"][i],
            label: dat["pathnames"][i],
            backgroundColor: colArr[i]
        };

        dataset.push(arr);
    }
    //console.log(step)

    var chartData = {
        datasets: [
            {
                label: "Enriched Pathways",
                data: dataset,
                backgroundColor: colArr,
                pointRadius: sizeArr,
                pointHoverRadius: hoverSizeArr
            }
        ]
    };

    const config = {
        type: 'scatter',
        data: chartData,
        options: {
            layout: {
                padding: 20
            },
            onClick: function (e, els) {
                var el = els[0];
                // 0:TR, 1:TL, 2:BL, 3:BR
                if (el.element !== undefined) {
                    myRemote([{name: 'pathname', value: el.element.$context.raw.label}]);
                }
            },

            scales: {
                y: {
                    title: {
                        display: true,
                        text: '-log10(P-value)'
                    },
                    type: 'linear',
                    grace: '5%'
                },
                x: {
                    title: {
                        display: true,
                        text: 'Enrichment Factor'
                    }
                }
            },

            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            var label = "";
                            //label = context.raw.label + ": " + context.raw.x + ", " + context.raw.y
                            label = [context.raw.label + ": ",
                                "Enrichment Factor:" + context.raw.x,
                                "-Log10 P-value:" + context.raw.y];

                            return label;
                        }
                    }
                },
                zoom: {
                    zoom: {
                        wheel: {
                            enabled: false
                        },
                        pinch: {
                            enabled: false
                        },
                        mode: 'xy',
                        drag: {
                            enabled: true,
                            borderColor: 'rgb(54, 162, 235)',
                            borderWidth: 1,
                            backgroundColor: 'rgba(54, 162, 235, 0.3)'
                        }
                    }
                }
            }
        }
    };

    myChart = new Chart(
            document.getElementById('canvas1'),
            config
            );

}


function showScatter2DGSEA(dat) {

    var up_count = 0;
    var down_count = 0;

    for (var i = 0; i < dat['enr'].length; i = i + 1) {
        if (dat['enr'][j] < 0) {
            down_count = down_count + 1;
        } else {
            up_count = up_count + 1;
        }
    }

    var arrlength = dat['enr'].length;
    var min = Math.min(...dat["enr"]);
    var max = Math.max(...dat["enr"]);
    var rainbow1 = generateColorGradient(["#458B00", "#fffee0"], down_count, min, 0);
    var rainbow2 = generateColorGradient(["#7f0000", "#fffee0"], up_count, 0, max);
    var hex_arr = [];

    var j = 0;
    for (var i = min; i <= max; i = i + (max - min) / arrlength) {
        if (dat['enr'][j] < 0) {
            hex_arr.push('#' + rainbow1.colourAt(i));
        } else {
            hex_arr.push('#' + rainbow2.colourAt(i));
        }
        j = j + 1;
    }

    var sizeArr = generateSizeGradient(dat["pval"], 5, 20);

    var data_obj = {Down: [], Up: []};
    var col_obj = {Down: [], Up: []};
    var size_obj = {Down: [], Up: []};
    var hover_size_obj = {Down: [], Up: []};

    for (var i = 0; i < dat["enr"].length; i++) {
        var arr = {
            x: dat["enr"][i],
            y: dat["pval"][i],
            size: sizeArr[i],
            label: dat["pathnames"][i],
            backgroundColor: hex_arr[i]
        };

        if (arr.x < 0) {
            data_obj["Down"].push(arr);
            col_obj["Down"].push(arr.backgroundColor);
            size_obj["Down"].push(arr.size);
            hover_size_obj["Down"].push(arr.size * 1.1);

        } else {
            data_obj["Up"].push(arr);
            col_obj["Up"].push(arr.backgroundColor);
            size_obj["Up"].push(arr.size);

            hover_size_obj["Up"].push(arr.size * 1.1);

        }
    }

    //console.log(step)

    var chartData = {
        datasets: [
            {
                label: "Downregulated",
                data: data_obj["Down"],
                backgroundColor: col_obj["Down"],
                pointRadius: size_obj["Down"],
                pointHoverRadius: size_obj["Down"]
            },
            {
                label: "Upregulated",
                data: data_obj["Up"],
                backgroundColor: col_obj["Up"],
                pointRadius: size_obj["Up"],
                pointHoverRadius: size_obj["Up"]
            },
        ]
    }

    const config = {
        type: 'scatter',
        data: chartData,
        options: {
            layout: {
                padding: 20
            },
            onClick: function (e, els) {
                var el = els[0];
                // 0:TR, 1:TL, 2:BL, 3:BR
                if (el.element !== undefined) {
                    myRemote([{name: 'pathname', value: el.element.$context.raw.label}]);
                }
            },

            scales: {
                y: {
                    title: {
                        display: true,
                        text: '-log10(P-value)'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'NES'
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            var label = "";
                            //label = context.raw.label + ": " + context.raw.x + ", " + context.raw.y
                            label = [context.raw.label + ": ",
                                "NES:" + context.raw.x,
                                "-Log10 P-value:" + context.raw.y];

                            return label;
                        }
                    }
                },
                zoom: {
                    zoom: {
                        wheel: {
                            enabled: false
                        },
                        pinch: {
                            enabled: false
                        },
                        mode: 'xy',
                        drag: {
                            enabled: true,
                            borderColor: 'rgb(54, 162, 235)',
                            borderWidth: 1,
                            backgroundColor: 'rgba(54, 162, 235, 0.3)'
                        }
                    },

                }
            }
        }
    };

    myChart = new Chart(
            document.getElementById('canvas1'),
            config
            );

}


function showScatter2DInteg(dat) {
    var colArr = [];

    var arrlength = dat['pval'].length;
    var rainbow = generateColorGradient(["#ff0000", "#FFEF00", "#ffffff"], arrlength, 0, arrlength);
    for (var i = 0; i < arrlength; i++) {
        colArr.push('#' + rainbow.colourAt(i));
    }

    var sizeArr = generateSizeGradient(dat["metap"], 5, 20);
    var hoverSizeArr = generateSizeGradient(dat["metap"], 6, 24);

    var dataset = [];
    for (var i = 0; i < dat["enr"].length; i++) {
        var arr = {
            x: dat["enr"][i],
            y: dat["pval"][i],
            label: dat["pathnames"][i],
            backgroundColor: colArr[i]
        };

        dataset.push(arr);
    }


    //console.log(step)

    var chartData = {
        datasets: [
            {
                label: "Enriched Pathways",
                data: dataset,
                backgroundColor: colArr,
                pointRadius: sizeArr,
                pointHoverRadius: hoverSizeArr
            }
        ]
    };

    const config = {
        type: 'scatter',
        data: chartData,
        options: {
            layout: {
                padding: 20
            },
            onClick: function (e, els) {
                var el = els[0];
                // 0:TR, 1:TL, 2:BL, 3:BR
                if (el.element !== undefined) {
                    myRemote([{name: 'pathname', value: el.element.$context.raw.label}]);
                }
            },

            scales: {
                y: {
                    title: {
                        display: true,
                        text: '-log10(p) Mummichog'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: '-log10(p) GSEA'
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            var label = "";
                            //label = context.raw.label + ": " + context.raw.x + ", " + context.raw.y
                            label = [context.raw.label + ": ",
                                "-log10(p) GSEA (scaled):" + context.raw.x,
                                "-log10(p) Mummichog (scaled):" + context.raw.y];

                            return label;
                        }
                    }
                },
                zoom: {
                    zoom: {
                        wheel: {
                            enabled: false
                        },
                        pinch: {
                            enabled: false
                        },
                        mode: 'xy',
                        drag: {
                            enabled: true,
                            borderColor: 'rgb(54, 162, 235)',
                            borderWidth: 1,
                            backgroundColor: 'rgba(54, 162, 235, 0.3)'
                        }
                    },

                }
            }
        }
    };

    myChart = new Chart(
            document.getElementById('canvas1'),
            config
            );

}

function generateColorGradient(colArr, arrlength, rangeMin, rangeMax) {
    var rainbow = new Rainbow();
    rainbow.setSpectrumByArray(colArr); //["#ff0000", "#FFEF00", "#ffffff"]
    var hex_arr = [];
    rainbow.setNumberRange(rangeMin, rangeMax);
    return rainbow;
}

function generateSizeGradient(origArr, newMin, newMax) {
    var sizeRescaled = [];
    for (var i = 0; i < origArr.length; i++) {
        var val = rescale2Range(origArr[i], Math.max(...origArr), Math.min(...origArr), newMax, newMin);
        sizeRescaled.push(val);
    }
    return sizeRescaled;

}
