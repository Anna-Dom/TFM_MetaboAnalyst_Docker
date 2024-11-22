var loggedIn = "false";
var metaUploaded = false;
var uploadPermission = "false";

document.addEventListener("DOMContentLoaded", function () {
    var uploadWidget = PF('importFile');
    uploadWidget.cancelButton[0].style.display="none";
    uploadWidget.upload = function (c)
    {
        if (loggedIn === "false") {
            PF('statusDialog').show();
        }
        
        /* --New function to handle file uplaoding and show the status dialog for chrome and opera --*/
        var thisobj = this;
        
        setTimeout(function(thisobj){
            loggedIn = doSpecLogin();
            //console.log("thisobj: " + thisobj);
            if (loggedIn !== "true") {
                return;
            } else {
                PF('statusDialog').hide();
                uploadPermission = checkUploadPermission();
                
                if (uploadPermission === "false") {
                    PF('uploadStatusDialog').show();
                    setTimeout(refresh, 5000);
                } else {
                    if (!uploadMeta()) {
                        uploadWidget.enableButton(uploadWidget.uploadButton);
                        return;
                    }
                    
                    if (thisobj.files[0] === undefined || thisobj.files[0] === null) {
                        metaUploaded = false;
                        uploadWidget.files = tempArr;
                        //return;
                    }
                    
                    if (thisobj.files[0].name.includes(".txt")) {
                        metaUploaded = true;
                    } else {
                        PF('importFile').files = tempArr
                    }
                    rc([{name: 'size', value: uploadWidget.files.length}]);
                    PrimeFaces.widget.FileUpload.prototype.upload.call(thisobj);
                }
            }
        }, 0, thisobj);
        /* Bottom of new function -------------------------------------*/
        
////        /* ------- OLD CODE in case -------*/
//        loggedIn = doSpecLogin();
//        if (loggedIn !== "true") {
//            return;
//        } else {
//            PF('statusDialog').hide();
//            uploadPermission = checkUploadPermission();
//            if (uploadPermission === "false") {
//                PF('uploadStatusDialog').show();
//                setTimeout(refresh, 5000);
//            } else {
//                if(!uploadMeta()){
//                    uploadWidget.enableButton(uploadWidget.uploadButton);
//                    return;
//                }
//                console.log("this.files: " + this);
//                
//               if (this.files[0] === undefined || this.files[0] === null) {
//                    metaUploaded = false;
//                    uploadWidget.files = tempArr;
//                    //return;
//                }
//                console.log("this.files[0]2: " + this.files[0]);
//                console.log("this.files[1]2: " + this.files[1]);
//                if (this.files[0].name.includes(".txt")) {
//                    metaUploaded = true;
//                }else{
//                    PF('importFile').files = tempArr
//                }
//                //console.log(uploadWidget.files);
//                rc([{name: 'size', value: uploadWidget.files.length}]);
//                PrimeFaces.widget.FileUpload.prototype.upload.call(this);
//            }
//        }
////           /* ------- OLD CODE in case -------*/
    };

    function swapElement(array, indexA, indexB) {
        var tmp = array[indexA];
        array[indexA] = array[indexB];
        array[indexB] = tmp;
    }

});

function sanityCheckMeta() {
    if (metaUploaded) {
        getMetaIntegrity();
        metaUploaded = false;
    }
}

function checkUploadPermission() {
    res = "false";
    $.ajax({
        beforeSend: function () {

        },
        dataType: "html",
        type: "GET",
        url: '/MetaboAnalyst/faces/AjaxCall',
        data: "function=checkUploadPermission",
        async: false,
        cache: false,
        success: function (result) {
            res = result;
        },
        error: function () {

        }
    });
    return(res);
}

function doSpecLogin() {
    var res = -100;
    $.ajax({
        beforeSend: function () {

        },
        dataType: "html",
        type: "GET",
        url: '/MetaboAnalyst/faces/AjaxCall',
        data: "function=doSpecLogin",
        async: false,
        cache: false,
        success: function (result) {
            res = result;
        },
        error: function () {

        }
    });
    return(res);
}

function getMetaIntegrity() {    
    var res = -100;
    var filenmsarr = "";
    for (var i = 0; i < tempArr.length; i++) {
        if (i == 0) {
            filenmsarr = tempArr[i].name;
        } else {
            filenmsarr = filenmsarr + "; " + tempArr[i].name;
        }
    }

    $.ajax({
        beforeSend: function () {

        },
        dataType: "html",
        type: "GET",
        url: '/MetaboAnalyst/faces/AjaxCall',
        data: {function: 'getMetaIntegrity', fileNms: filenmsarr},
        async: false,
        cache: false,
        success: function (result) {
            
            var uploadWidget = PF('importFile');
            uploadWidget.files = tempArr;
            tempArr = [];
            
            //metadata subset
            //include meta-data radiobutton
            if (result === "true") {
                if (uploadPermission === "false") {
                    PF('uploadStatusDialog').show();
                    setTimeout(refresh, 5000);
                } else {
                    rc([{name: 'size', value: uploadWidget.files.length}]);
                    PrimeFaces.widget.FileUpload.prototype.upload.call(uploadWidget);
                }
            } else {
                metaUploaded = false;
                showMetaErrorRC();
                uploadWidget.enableButton(uploadWidget.cancelButton);
            }
        },
        error: function () {

        }
    });
    return(res);
}

function uploadMeta() {
    var res = checkSessionUploaded();
    
    if(res){
        PF('uploadSessionDialog').show();
        return false;
    }
    var arr = PF('importFile').files;
    tempArr = [];
    var inx;
    var metaFile;
    var j = 0;
    //two meta files 
    for (var i = 0; i < arr.length; i++) {
        var file = arr[i];
        if (file.name.includes(".txt")) {
            inx = i;
            metaFile = arr[i];
            j++
        } else {
            tempArr.push(arr[i]);
        }
    }
    
    if(j > 1){
        moreThanOneMetaRC();
        return false;
    }else if (metaFile !== null) {
        PF('importFile').files = [metaFile];
        return true;
    }
}

function refresh() {
    uploadPermission = checkUploadPermission();
    if (uploadPermission === "false") {
        setTimeout(refresh, 5000);
    } else {
        rc([{name: 'size', value: PF('importFile').files.length}]);
        PrimeFaces.widget.FileUpload.prototype.upload.call(PF('importFile'));
        PF('uploadStatusDialog').hide();
    }
}

function checkSessionUploaded(){
    var res = false;
    $.ajax({
        beforeSend: function () {

        },
        dataType: "html",
        type: "GET",
        url: '/MetaboAnalyst/faces/AjaxCall',
        data: "function=checkSessionUploaded",
        async: false,
        cache: false,
        success: function (result) {
            res = result;
            if(res === "true"){
                res = true;
            }else{
                res = false;
            }
           // console.log(res)
        },
        error: function () {

        }
    });
    return res;
}