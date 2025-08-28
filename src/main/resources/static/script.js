/**
 * 
 */

const search_kw = document.getElementById("search_kw");
if (search_kw != null) {
	search_kw.addEventListener('keypress', function() {
		if (event.keyCode === 13) {
			document.getElementById('kw').value = this.value;
			document.getElementById('page').value = 0;
			document.getElementById('searchForm').submit();
		}
	});
}



const viewerElement = document.querySelector('#viewer');
if (viewerElement) {
	//	const viewer = new 
	toastui.Editor.factory({
		el: viewerElement,
		viewer: true,
		initialValue: document.querySelector('#ecp1').value
	});
}