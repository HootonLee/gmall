
function searchES() {
    var keyword = $("#keyword").val();
    keyword = keyword.replace(/\s+/g, "");
    if (keyword!=null && keyword.length>0){
        window.location.href = "http://search.gmall.com:8083/list.html?keyword="+keyword;
    }
}

function item(id) {
    var skuInfoId = id;
    if (id != null && id != ""){
        window.location.href = "http://item.gmall.com:8082/"+id+".html";
    }
}