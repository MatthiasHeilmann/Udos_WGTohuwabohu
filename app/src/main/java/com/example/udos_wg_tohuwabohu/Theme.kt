package com.example.udos_wg_tohuwabohu

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.lightColors
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

val Red700 = Color(0xffdd0d3c)
val Red800 = Color(0xffd00036)
val Red900 = Color(0xffc20029)

val UdoLightBlue = Color(0xff30475e)
val UdoDarkBlue = Color(0xff222831)
val UdoGray = Color(0xffdddddd)
val UdoOrange = Color(0xffe29e21)
val UdoRed = Color(0xfff05454)
val UdoDarkGray= Color(0xFF313131)

val Typography.popupLabel: TextStyle
    get() = TextStyle(fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.W600,
        fontSize = 20.sp)

@Composable
fun UdosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme().copy(primary = UdoDarkBlue, secondary = UdoGray),
        content = content,
        typography = Typography(
            displayMedium = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.W600,
                fontSize = 30.sp
            )
        )
    )
}

@Composable
fun UdoCardTheme(): CardColors {
    val UdoCardColors= cardColors(containerColor= UdoDarkBlue, contentColor= UdoGray, disabledContainerColor= Color.Black, disabledContentColor= Color.Magenta)
    return UdoCardColors
}

@Composable
fun UdoPopupCardTheme(): CardColors {
    val UdoPopupCardColors = cardColors(
        containerColor = UdoLightBlue,
        contentColor = UdoGray,
        disabledContainerColor = Color.Black,
        disabledContentColor = Color.Magenta
    )
    return UdoPopupCardColors
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UdoPopupTextfieldColors(): TextFieldColors{
    val UdoPopupTextfieldColors= TextFieldDefaults.textFieldColors(textColor = UdoDarkGray, containerColor = UdoGray, cursorColor = UdoDarkGray)
    return UdoPopupTextfieldColors
}

@Composable
fun UdoDateCardTheme(): CardColors {
    val UdoDateCardColors= cardColors(containerColor= UdoLightBlue, contentColor= UdoGray, disabledContainerColor= Color.Black, disabledContentColor= Color.Magenta)
    return UdoDateCardColors
}

@Composable
fun UdoKeyboardOptions(): KeyboardOptions {
    val UdoKeyboardOptions= KeyboardOptions(
        capitalization =   KeyboardCapitalization.Sentences ,
        autoCorrect = false,
            keyboardType = KeyboardType.Text ,
            imeAction = ImeAction.Default
    )
    return UdoKeyboardOptions
}

fun UdoPopupProperties(): PopupProperties{
  val UdoPopupProperties= PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true)
    return UdoPopupProperties
}







