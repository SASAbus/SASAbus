function setTileLayerForMap(map) {
    if (Android.shouldUseOnlineMap()) {
        console.log("Using online map tiles");

        L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>',
            maxZoom: 17,
            minZoom: 8
        }).addTo(map);
    } else {
        console.log("Using offline map tiles");

        var mapPath = Android.getMapTilesRootUrl() + "/{z}/{x}/{y}.png";

        L.tileLayer(mapPath, {
            attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors',
            maxZoom: 17,
            minZoom: 8
        }).addTo(map);
    }
}