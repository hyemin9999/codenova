/**
 * 
 */

const kw = document.getElementById("search_field");

const page = document.getElementById("search_field");
const field = document.getElementById("search_field");
const search_field = document.getElementById("search_field");
const searchForm = document.getElementById('searchForm');
/**
 * 검색 버튼 처리
 */

const search_kw = document.getElementById("search_kw");
if (search_kw != null) {
	search_kw.addEventListener('keypress', function() {
		if (event.keyCode === 13) {
			kw.value = this.value;
			page.value = 0;
			field.value = search_field.value;
			searchForm.submit();
		}
	});
}

const ecp1 = document.querySelector('#ecp1');
const viewerElement = document.querySelector('#viewer');
if (viewerElement) {
	//	const viewer = new 
	toastui.Editor.factory({
		el: viewerElement,
		viewer: true,
		initialValue: ecp1.value
	});
}