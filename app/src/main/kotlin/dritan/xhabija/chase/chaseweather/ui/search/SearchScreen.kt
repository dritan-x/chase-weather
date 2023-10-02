package dritan.xhabija.chase.chaseweather.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dritan.xhabija.chase.chaseweather.ui.home.WeatherViewModel
import dritan.xhabija.chase.chaseweather.location.LocationViewModel
import dritan.xhabija.chase.chaseweather.ui.theme.MainScreenSearchField
import dritan.xhabija.chase.chaseweather.ui.theme.Pink40
import dritan.xhabija.chase.chaseweather.ui.theme.PurpleGrey40
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onNavigateToHome: () -> Unit, onNavigateToSettings: () -> Unit) {
    val searchViewModel = koinViewModel<SearchViewModel>()
    val weatherViewModel = koinViewModel<WeatherViewModel>()
    val locationViewModel = koinViewModel<LocationViewModel>()

    // get last known location
    val currLocation = locationViewModel.getCurrentLocationFlow().collectAsState().value
    val lastSearchedQuery = searchViewModel.getLastSearchedQueryFlow().collectAsState().value
    val recentSearches = searchViewModel.getRecentSearchesFlow().collectAsState().value
    // get weather conditions returned by WeatherApi calls
    val weatherConditions = weatherViewModel.weatherConditions.collectAsState().value

    val madeQuery = remember { mutableStateOf(false) }
    if (madeQuery.value && weatherConditions.isNotEmpty()){
        onNavigateToHome()
        return
    }

    var searchFieldValue by remember { mutableStateOf(lastSearchedQuery) }


    Column {
        Row {
            Button(
                modifier = Modifier.weight(0.5F),
                onClick = {
                    onNavigateToHome()
                },
            ) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
            }

            OutlinedTextField(
                modifier = Modifier
                    .width(200.dp)
                    .height(55.dp)
                    .weight(2F)
                    .border(
                        width = 3.dp,
                        color = Color.Black
                    ),
                value = searchFieldValue,
                onValueChange = { newText ->
                    searchFieldValue = newText
                },

                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                trailingIcon = {
                    Icon(Icons.Default.Clear,
                        contentDescription = "clear typed text",
                        modifier = Modifier
                            .clickable {
                                searchFieldValue = ""
                            }
                    )
                }
            )

            Button(
                modifier = Modifier
                    .weight(0.7F)
                    .height(55.dp)
                    .background(MainScreenSearchField)
                    .border(
                        width = 3.dp,
                        color = Color.Black
                    )
                    .testTag("GoButton"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainScreenSearchField,
                    contentColor = Color.Black
                ),
                shape = RectangleShape,
                onClick = {
                    madeQuery.value = true
                    weatherViewModel.clearDeviceLocation()
                    // trigger fetching weather for search query, listen for changes in weatherConditions above
                    weatherViewModel.getWeatherForSearchQueryFlow(searchFieldValue)
                    searchViewModel.setLastSearchQuery(searchFieldValue)
                },
            ) {
                Text("Go!")
            }
        }

        // current location
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 10.dp)
                .drawBehind {
                    val strokeWidthPx = 1.dp.toPx()
                    val verticalOffset = size.height - 2.sp.toPx()
                    drawLine(
                        color = PurpleGrey40,
                        strokeWidth = strokeWidthPx,
                        start = Offset(0f, verticalOffset),
                        end = Offset(size.width-80, verticalOffset)
                    )
                }
                .clickable {
                    madeQuery.value = true
                    weatherViewModel.getWeatherForSimpleLocationFlow(currLocation, true)
                }
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Current location",
                modifier = Modifier
                    .weight(0.25F)
            )
            Text(
                text = "Current Location: ${currLocation.lat},${currLocation.lon} " ,
                style = TextStyle(fontSize = 14.sp, color = Pink40),
                modifier = Modifier
                    .weight(4F),
                fontSize = 16.sp
            )

        }

        Column (
            modifier = Modifier.padding(10.dp)
        ){
            Text(text = "Recent Searches", style = TextStyle(color = Color.Gray, fontSize = 18.sp))
            recentSearches.forEach { query ->
                Text(
                    modifier = Modifier.clickable {
                        madeQuery.value = true
                        weatherViewModel.getWeatherForSearchQueryFlow(query)
                        searchViewModel.setLastSearchQuery(query)
                    },
                    text = query
                )
            }
        }
    }
}

