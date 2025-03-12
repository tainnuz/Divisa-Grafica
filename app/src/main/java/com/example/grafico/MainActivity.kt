package com.example.grafico

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
import com.example.grafico.data.local.Divisa
import com.example.grafico.ui.theme.GraficoTheme
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Variación del Tipo de Cambio", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (divisas.isNotEmpty()) {
            GraficoDivisas(divisas)
        } else {
            Text(text = "Cargando datos...")
        }
    }
}

@Composable
fun GraficoDivisas(divisas: List<Divisa>) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description = Description().apply { text = "Tasa de Cambio" }
                data = obtenerDatosGrafico(divisas)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

fun obtenerDatosGrafico(divisas: List<Divisa>): LineData {
    val entries = divisas.mapIndexed { index, divisa ->
        Entry(index.toFloat(), divisa.valor.toFloat()) // Conversión para el gráfico
    }

    val dataSet = LineDataSet(entries, "Valor en MXN").apply {
        colors = ColorTemplate.COLORFUL_COLORS.toList()
        valueTextSize = 12f
    }

    return LineData(dataSet)
}
