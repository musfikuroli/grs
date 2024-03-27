$.fn.datepicker.dates['bn'] = {
	days: ["রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"],
	daysShort: ["রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি"],
	daysMin: ["র", "সো", "ম", "বু", "বৃ", "শু", "শ"],
	months: ["জানুয়ারী", "ফেব্রুয়ারী", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"],
	monthsShort: ["জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ", "অক্টো", "নভে", "ডিসে"],
	today: "আজ",
	clear: "পরিষ্কার",
	format: "dd/mm/yyyy",
	titleFormat: "MM yyyy", /* Leverages same syntax as 'format' */
	weekStart: 6/*,
	beforeShowDay: function(date) {
		console.log(date);
		console.log(convertToBanglaNumbers(date));
		return convertToBanglaNumbers(date);
	}*/
};

/*
var symbolMap = {
	'1': '১',
	'2': '২',
	'3': '৩',
	'4': '৪',
	'5': '৫',
	'6': '৬',
	'7': '৭',
	'8': '৮',
	'9': '৯',
	'0': '০'
},
numberMap = {
	'১': '1',
	'২': '2',
	'৩': '3',
	'৪': '4',
	'৫': '5',
	'৬': '6',
	'৭': '7',
	'৮': '8',
	'৯': '9',
	'০': '0'
};

var bNumber = new Array("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯");
var eNumber = new Array("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

function convertToBanglaNumbers(date) {
	if ($(date)) {		
		var len = $(date).trim().length;
		var prependStr = "1";
		if (len < 1) {
			// just show the error
		} else if (len < 10) {
			var prePadLength = (10 - len) - 1;
			for (i=0; i < prePadLength; i++) {
				prependStr += "0";
			}
			$(this).val(prependStr + $(this).val().trim());
		}
	} else {
		var tempNumber = "";
		var number = date;
		for(a = 0; a < bNumber.length; a++) {
			for (b = 0; b < number.length; b++) {
				number = number.replace(bNumber[a], eNumber[a]);
			}
		}
		
		number = parseInt(number);
		
		var len = $(this).val().trim().length;
		var prependStr = "1";
		if (!isNaN(number)) {
			if (len < 1) {
				// just show the error
			} else if (len < 10) {
				var prePadLength = (10 - len) - 1;
				for (i=0; i < prePadLength; i++) {
					prependStr += "0";
				}
				date = (prependStr + number);
			}
		}
	}
	return "abc";
}
*/