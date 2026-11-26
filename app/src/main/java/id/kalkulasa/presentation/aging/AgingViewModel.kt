package id.kalkulasa.presentation.aging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.model.Receivable
import id.kalkulasa.domain.repository.ReceivableRepository
import id.kalkulasa.domain.usecase.PiutangAgingUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AgingViewModel @Inject constructor(
    receivableRepository: ReceivableRepository,
) : ViewModel() {

    val items: StateFlow<List<Receivable>> = receivableRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totals: StateFlow<Map<PiutangAgingUseCase.Bucket, Long>> = items
        .map { list ->
            val now = System.currentTimeMillis()
            PiutangAgingUseCase.totals(
                list.filter { !it.isLunas }
                    .map { Triple(it.sisa, it.jatuhTempo, now) },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val outstanding: StateFlow<Long> = receivableRepository.observeOutstanding()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
}
