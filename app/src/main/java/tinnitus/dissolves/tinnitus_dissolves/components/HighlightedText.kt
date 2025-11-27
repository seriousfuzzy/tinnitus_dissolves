package tinnitus.dissolves.tinnitus_dissolves.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HighlightedText(
    text: String,
    highlightColor: Color,
    fontSize: TextUnit
) {
    val startIndex = text.indexOf(text)
    val endIndex = startIndex + text.length

    val annotatedString = buildAnnotatedString {
        append(text)
        if (startIndex >= 0) {
            addStyle(
                style = SpanStyle(
                    background = highlightColor,
                    fontWeight = FontWeight.Bold
                ),
                start = startIndex,
                end = endIndex
            )
        }
    }

    Text(
        text = annotatedString,
        fontSize = fontSize,
        color = Color.Black
    )
}

@Composable
fun IndentText(indent: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = indent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}

@Composable
fun IndentAndStyleText(indent: String, fullText: String, boldPart: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = indent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        StyledText(
            fullText, boldPart
        )
    }
}

@Composable
fun StyledText(fullText: String, boldPart: String) {

    val annotatedString = buildAnnotatedString {
        val startIndex = fullText.indexOf(boldPart)
        val endIndex = startIndex + boldPart.length

        append(fullText)

        addStyle(
            style = SpanStyle(fontWeight = FontWeight.Bold),
            start = startIndex,
            end = endIndex
        )
    }

    Text(
        text = annotatedString,
        fontSize = 20.sp,
        color = Color.Black,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun Body(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun BoldBody(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}