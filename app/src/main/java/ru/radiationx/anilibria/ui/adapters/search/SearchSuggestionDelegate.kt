package ru.radiationx.anilibria.ui.adapters.search

import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lapism.searchview.SearchView
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_fast_search.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.search.FastSearchItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchSuggestionListItem
import java.util.regex.Pattern

/**
 * Created by radiationx on 13.01.18.
 */
class SearchSuggestionDelegate : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is SearchSuggestionListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as SearchSuggestionListItem
        (holder as ViewHolder).bind(item.item, item.query)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_fast_search, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener {
                Log.e("loalao", "onclick")
            }
        }

        fun bind(item: FastSearchItem, query: String) {
            view.run {
                ImageLoader.getInstance().cancelDisplayTask(item_poster)
                ImageLoader.getInstance().displayImage(item.poster, item_poster)
                val title = item.names.joinToString(" / ")

                val matcher = Pattern.compile(query, Pattern.CASE_INSENSITIVE).matcher(title)
                val s = SpannableString(title)
                while (matcher.find()) {
                    s.setSpan(StyleSpan(Typeface.BOLD), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                item_title.setText(s, TextView.BufferType.SPANNABLE)
            }
        }
    }
}