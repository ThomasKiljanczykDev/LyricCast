/*
 * Created by Tomasz Kiljanczyk on 4/20/21 1:10 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 12:11 AM
 */

package pl.gunock.lyriccast.fragments.viewholders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument

class EditCategoryDialogViewModel(
    val categoryNames: MutableLiveData<Set<String>> = MutableLiveData(setOf()),
    var category: MutableLiveData<CategoryDocument> = MutableLiveData<CategoryDocument>(),
) : ViewModel()