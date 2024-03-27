var locationPlugin = function () {
    $("#divisionSelect").select2({width: '208px'});
    $("#districtSelect").select2({width: '208px'});
    $("#upazillaSelect").select2({width: '208px'});

    $('#divisionSelect').on('change', function () {
        if (languageCode == "en") {
            $('#districtSelect').empty().append("<option value='0'>Select District</option>");
            $('#upazillaSelect').empty().append("<option value='0'>Select Upazila</option>");
        } else {
            $('#districtSelect').empty().append("<option value='0'>জেলা নির্বাচন করুন</option>");
            $('#upazillaSelect').empty().append("<option value='0'>উপজেলা নির্বাচন করুন</option>");
        }

        if ($('#divisionSelect').val() != "" && $('#divisionSelect').val() != null
            && $("#divisionSelect").prop('selectedIndex') > 0) {
            loadDistricts($('#divisionSelect').val())
        }
    });

    $('#districtSelect').on('change', function () {
        if (languageCode == "en") {
            $('#upazillaSelect').empty().append("<option value='0'>Select Upazila</option>");
        } else {
            $('#upazillaSelect').empty().append("<option value='0'>উপজেলা নির্বাচন করুন</option>");
        }

        if ($('#districtSelect').val() != "" && $('#districtSelect').val() != null
            && $("#districtSelect").prop('selectedIndex') > 0) {
            loadUpazilas($('#districtSelect').val())
        }
    });
}

var loadDivisions = function () {
    $.getJSON("/api/geo/division", function(result) {
        if (languageCode == "en") {
            $('#divisionSelect').empty().append("<option value='0'>Select Division</option>");
            $('#districtSelect').empty().append("<option value='0'>Select District</option>");
            $('#upazillaSelect').empty().append("<option value='0'>Select Upazila</option>");
        } else {
            $('#divisionSelect').empty().append("<option value='0'>বিভাগ নির্বাচন করুন</option>");
            $('#districtSelect').empty().append("<option value='0'>জেলা নির্বাচন করুন</option>");
            $('#upazillaSelect').empty().append("<option value='0'>উপজেলা নির্বাচন করুন</option>");
        }
        $.each(result, function(i, field) {
            if (languageCode == "en") {
                $('#divisionSelect').append("<option value=" + field.id+ "> " + field.nameEnglish + "</option>")
            } else {
                $('#divisionSelect').append("<option value=" + field.id+ "> " + field.nameBangla + "</option>")
            }
        });
    });
}

var loadDistricts = function (divisionId) {
    $.getJSON("/api/geo/district/" + divisionId, function(result) {
        if (languageCode == "en") {
            $('#districtSelect').empty().append("<option value='0'>Select District</option>");
        } else {
            $('#districtSelect').empty().append("<option value='0'>জেলা নির্বাচন করুন</option>");
        }
        $.each(result, function(i, field) {
            if (languageCode == "en") {
                $('#districtSelect').append("<option value=" + field.id+ "> " + field.nameEnglish + "</option>")
            } else {
                $('#districtSelect').append("<option value=" + field.id+ "> " + field.nameBangla + "</option>")
            }
        });
    });
}

var loadUpazilas = function (districtId) {
    $.getJSON("/api/geo/upazilas/" + districtId, function(result) {
        if (languageCode == "en") {
            $('#upazillaSelect').empty().append("<option value='0'>Select Upazila</option>");
        } else {
            $('#upazillaSelect').empty().append("<option value='0'>উপজেলা নির্বাচন করুন</option>");
        }
        $.each(result, function(i, field) {
            if (languageCode == "en") {
                $('#upazillaSelect').append("<option value=" + field.id+ "> " + field.nameEnglish + "</option>")
            } else {
                $('#upazillaSelect').append("<option value=" + field.id+ "> " + field.nameBangla + "</option>")
            }
        });
    });
}