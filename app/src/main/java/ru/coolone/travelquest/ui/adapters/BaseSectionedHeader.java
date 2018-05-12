package ru.coolone.travelquest.ui.adapters;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by coolone on 15.11.17.
 */

@NoArgsConstructor
@AllArgsConstructor
public class BaseSectionedHeader implements Serializable {
    @Getter
    @Setter
    private String title;
}
