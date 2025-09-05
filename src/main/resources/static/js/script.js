/**
 * list.html - 검색 페이징관련
 */

const page_element = document.getElementById('page');
const kw_element = document.getElementById('kw');
const search_kw_element = document.getElementById('search_kw');
const field_element = document.getElementById('field');
const search_field_element = document.getElementById('search_field_select');
const searchForm = document.getElementById('searchForm');

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
const ecp1_element = document.querySelector('#ecp1');
const viewerElement = document.querySelector('#viewer');
if (viewerElement) {
	toastui.Editor.factory({
		el: viewerElement,
		viewer: true,
		initialValue: ecp1.value
	});
}

function onDisplay(d) {
	const pwd_element = document.getElementById("pwd");
	if (pwd_element) {
		if (!d) {
			pwd_element.style.display = 'flex';
		} else {
			pwd_element.style.display = 'none';
		}
	}

	const pwdchange_element = document.getElementById("pwdchange");
	const alert_elements = document.getElementsByClassName("alert alert-danger");
	if (pwdchange_element) {
		if (d) {
			pwdchange_element.style.display = 'flex';
		} else {
			pwdchange_element.style.display = 'none';
			Array.from(alert_elements).forEach(function(element) {
				element.remove();
			});
		}
	}
}

/**
 * modal_show
 */
function modal_show(message) {
	$('#message').text(message);
	$('#modal').modal('show');
}

/**
 * form.html
 */
var files = new Array();
const failed_message = '이미지 업로드가 실패했습니다.';
const contents_element = document.querySelector('#contents')















