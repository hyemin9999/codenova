/**
 * list.html - 검색 페이징관련
 */

const page_element = document.getElementById('page');
const kw_element = document.getElementById('kw');
const search_kw_element = document.getElementById('search_kw');;
const field_element = document.getElementById('field');
const search_field_element = document.getElementById('search_field_select');
const searchForm = document.getElementById('searchForm');

// 페이지 번호/화살표 클릭 → page 값만 바꾸고 같은 폼 제출
const page_elements = document.getElementsByClassName("page-link");
if (page_elements.length > 0) {
	Array.from(page_elements).forEach(function(element) {
		element.addEventListener('click', function() {
			page_element.value = this.dataset.page;
			searchForm.submit();
		});
	});
}
const btn_search = document.getElementById("btn_search");
if (btn_search != null) {
	btn_search.addEventListener('click', function() {
		kw_element.value = search_kw_element.value;
		page_element.value = 0;
		field_element.value = search_field_element.value;
		searchForm.submit();
	});
}
if (search_kw_element != null) {
	search_kw_element.addEventListener('keypress', function() {
		if (event.keyCode === 13) {
			kw_element.value = this.value;
			page_element.value = 0;
			field_element.value = search_field_element.value;
			searchForm.submit();
		}
	});
}

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

const page_size_select = document.getElementById("page_size_select");
if (page_size_select != null) {
	// 페이지당 개수 바꾸면 1페이지부터 재조회
	page_size_select.addEventListener('change', function() {
		page.value = 0;
		searchForm.submit();
	});
}


/**
 * detail.html
 */
const delete_elements = document.getElementsByClassName("delete");
if (delete_elements.length > 0) {
	Array.from(delete_elements).forEach(function(element) {
		element.addEventListener('click', function() {
			if (confirm("정말로 삭제하시겠습니까?")) {
				location.href = this.dataset.uri;
			}
		});
	});
}

const recommend_elements = document.getElementsByClassName("recommend");
if (recommend_elements.length > 0) {
	Array.from(recommend_elements).forEach(function(element) {
		element.addEventListener('click', function() {
			if (confirm("정말로 추천하시겠습니까?")) {
				location.href = this.dataset.uri;
			}
		});
	});
}

const favorite_elements = document.getElementsByClassName("favorite");
if (favorite_elements.length > 0) {
	Array.from(favorite_elements).forEach(function(element) {
		element.addEventListener('click', function() {
			if (confirm("정말로 즐겨찾기하시겠습니까?")) {
				location.href = this.dataset.uri;
			}
		});
	});
}

/**
 * detail.html - editor
 */
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