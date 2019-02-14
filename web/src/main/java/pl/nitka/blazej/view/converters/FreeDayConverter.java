package pl.nitka.blazej.view.converters;

import pl.nitka.blazej.temporary.FreeDaysForMonth;
import pl.nitka.blazej.view.timetable.TimeTableGeneratePageService;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Konwerter obiektu reprezentującego święto do wygenerowania grafiku.
 *
 * @author Blazej
 */
@FacesConverter(value = "freeDayConverter")
public class FreeDayConverter implements Converter {

    @Override
    public FreeDaysForMonth getAsObject(FacesContext ctx, UIComponent uiComponent, String s) {
        ValueExpression vex = ctx.getApplication().getExpressionFactory().createValueExpression(ctx.getELContext(),
                "#{timeTableGeneratePageService}", TimeTableGeneratePageService.class);

        TimeTableGeneratePageService managedBean = (TimeTableGeneratePageService)vex.getValue(ctx.getELContext());
        return managedBean.convertFreeDay(s);
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent uiComponent, Object o) {
        if (o instanceof FreeDaysForMonth) {
            return ((FreeDaysForMonth) o).getLabel();
        }
        return "";
    }
}
