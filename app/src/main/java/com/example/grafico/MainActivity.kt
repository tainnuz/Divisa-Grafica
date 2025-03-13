package com.example.grafico

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grafico.data.Divisa
import com.example.grafico.ui.theme.GraficoTheme
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown

class MainActivity : ComponentActivity() {

    private val divisaViewModel: DivisaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraficoTheme {
                PantallaPrincipal(divisaViewModel)
            }
        }
        divisaViewModel.actualizarDivisas()
    }
}

@Composable
fun PantallaPrincipal(viewModel: DivisaViewModel = viewModel()) {
    val divisas by viewModel.divisas.observeAsState(emptyList())
    var selectedDivisa by remember { mutableStateOf<Divisa?>(null) }
    val historial by viewModel.getHistorialDivisa(selectedDivisa?.divisa ?: "").observeAsState(emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Cambio de Divisas", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de divisas
        if (divisas.isNotEmpty()) {
            DivisaSelector(divisas, onDivisaSelected = { divisa ->
                selectedDivisa = divisa
            })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar el mensaje con la tasa de cambio debajo del selector
        if (selectedDivisa != null) {
            val tasaCambio = "${1} MXN = ${selectedDivisa!!.valor} ${selectedDivisa!!.divisa}"
            Text(text = tasaCambio, style = MaterialTheme.typography.bodyMedium)

            // Mostrar gráfico
            GraficoDivisas(historial)
        } else{
            Text(text = "Cargando datos...")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DivisaSelector(divisas: List<Divisa>, onDivisaSelected: (Divisa) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("Selecciona una divisa") }

    // Selector de divisa como un ExposedDropdownMenu
    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            // Campo de texto que actúa como el select
            TextField(
                value = selectedText,
                onValueChange = { },
                readOnly = true,  // Hace que el TextField sea solo lectura
                label = { Text("") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Mostrar opciones"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                divisas.forEach { divisa ->
                    DropdownMenuItem(
                        onClick = {
                            selectedText = divisa.divisa
                            onDivisaSelected(divisa)
                            expanded = false
                        },
                        text = { Text(text = divisa.divisa) }
                    )
                }
            }
        }
    }
}

@Composable
fun GraficoDivisas(historial: List<Divisa>) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                setTouchEnabled(true)
                setPinchZoom(true)
                description = Description().apply {
                    text = "Historial de la Divisa"
                    textSize = 12f
                    textColor = Color.BLACK
                }

                // Configurar el eje X
                xAxis.apply {
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(value.toLong()))
                        }
                    }
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    textColor = Color.BLACK
                    textSize = 10f
                    axisLineColor = Color.GRAY
                    setDrawGridLines(false)
                }

                // Configurar el eje Y izquierdo
                axisLeft.apply {
                    textColor = Color.BLACK
                    textSize = 10f
                    axisLineColor = Color.GRAY
                    granularity = 1f
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                }

                axisRight.isEnabled = false

                // Configurar la leyenda
                legend.apply {
                    textColor = Color.BLACK
                    textSize = 12f
                    formSize = 12f
                    form = Legend.LegendForm.LINE
                }
            }
        },
        modifier = Modifier
            .fillMaxSize(),
        update = { lineChart ->
            if (historial.isNotEmpty()) {
                lineChart.data = obtenerDatosGrafico(historial)
                lineChart.invalidate()
                lineChart.animateX(1000)
                lineChart.animateY(1000)
            } else {
                lineChart.clear()
            }
        }
    )
}

fun obtenerDatosGrafico(historial: List<Divisa>): LineData {
    if (historial.isEmpty()) return LineData()

    val entries = historial.map { divisa ->
        // Convertir la fecha (timestamp) a un valor numérico para el eje X
        val fecha = divisa.fechaActualizacion.toFloat()
        Entry(fecha, divisa.valor.toFloat()) // Eje X: fecha, Eje Y: valor
    }

    val dataSet = LineDataSet(entries, "Valor en MXN").apply {
        color = Color.rgb(0, 150, 136)
        lineWidth = 2.5f
        setDrawCircles(true)
        circleColors = listOf(Color.rgb(0, 150, 136))
        circleRadius = 4f
        valueTextSize = 12f
        valueTextColor = Color.BLACK
        mode = LineDataSet.Mode.CUBIC_BEZIER
        cubicIntensity = 0.2f
        setDrawFilled(true)
        fillColor = Color.rgb(0, 150, 136)
        fillAlpha = 50
    }

    return LineData(dataSet)
}
