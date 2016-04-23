/*
 * Copyright (C) 2015 Karumi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karumi.katasuperheroes;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import com.karumi.katasuperheroes.di.MainComponent;
import com.karumi.katasuperheroes.di.MainModule;
import com.karumi.katasuperheroes.matchers.RecyclerViewItemsCountMatcher;
import com.karumi.katasuperheroes.model.SuperHero;
import com.karumi.katasuperheroes.model.SuperHeroesRepository;
import com.karumi.katasuperheroes.ui.view.MainActivity;
import it.cosenonjaviste.daggermock.DaggerMockRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class) @LargeTest public class MainActivityTest {

    @Rule public DaggerMockRule<MainComponent> daggerRule =
            new DaggerMockRule<>(MainComponent.class, new MainModule()).set(
                    new DaggerMockRule.ComponentSetter<MainComponent>() {
                        @Override public void setComponent(MainComponent component) {
                            SuperHeroesApplication app =
                                    (SuperHeroesApplication) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Rule public IntentsTestRule<MainActivity> activityRule =
            new IntentsTestRule<>(MainActivity.class, true, false);

    @Mock SuperHeroesRepository repository;

    @Test public void showsEmptyCaseIfThereAreNoSuperHeroes() {
        givenThereAreNoSuperHeroes();

        startActivity();

        onView(withText("¯\\_(ツ)_/¯")).check(matches(isDisplayed()));
    }

    @Test public void doesNotShowEmptyCaseIfThereAreSomeSuperHeroes() {
        givenThereAreSomeSuperHeroes();

        startActivity();

        onView(withId(R.id.tv_empty_case)).check(matches(not(isDisplayed())));
    }

    @Test public void showTheNumberOfSuperHeroes() {
        givenThereAreANumberOfSuperHeroes(10);

        startActivity();

        onView(withId(R.id.recycler_view)).check(
                matches(RecyclerViewItemsCountMatcher.recyclerViewHasItemCount(10)));
    }

    @Test public void showSuperHeroesName() {

        int superHeroesName = 1000;
        givenThereAreANumberOfSuperHeroes(superHeroesName);

        startActivity();

        for (int i = 0; i < superHeroesName; i++) {
            onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(i));
            onView(withText("Hero " + i)).check(matches(isDisplayed()));
        }
    }

    @Test public void showAvengersBadgeWhenSuperHeroIsAvenger() {

        int superHeroesName = 1000;

        givenThereAreANumberOfSuperHeroesWhereOddAreAvengers(superHeroesName);

        startActivity();

        for (int i = 0; i < superHeroesName; i++) {
            onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(i));

            onView(allOf(withId(R.id.iv_avengers_badge), hasSibling(withText("Hero " + i)))).check(
                    matches(i % 2 == 0 ? isDisplayed() : not(isDisplayed())));
        }
    }

    @Test public void showSuperHeroDetailWhenSuperHeroClickedInList() {

        String HERO_NAME = "Hero 0";

        List<SuperHero> superHeros = givenThereAreSomeSuperHeroes();
        when(repository.getByName(HERO_NAME)).thenReturn(superHeros.get(0));

        startActivity();

        onView(withId(R.id.recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView( withId(R.id.tv_super_hero_name)).check(matches(withText(HERO_NAME)) );

    }

    @Test public void showAvengerDetailWhenAvengerClickedInList() {

        String HERO_NAME = "Hero 0";

        List<SuperHero> superHeros = givenThereAreANumberOfSuperHeroesWhereOddAreAvengers(5);
        when(repository.getByName(HERO_NAME)).thenReturn(superHeros.get(0));

        startActivity();

        onView(withId(R.id.recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView( withId(R.id.iv_avengers_badge)).check(matches(isDisplayed()) );
    }


    @Test public void doesNotShowAvengerDetailWhenAvengerClickedInList() {

        String HERO_NAME = "Hero 1";

        List<SuperHero> superHeros = givenThereAreANumberOfSuperHeroesWhereOddAreAvengers(5);
        when(repository.getByName(HERO_NAME)).thenReturn(superHeros.get(1));

        startActivity();

        onView(withId(R.id.recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(1, click()));

        onView( withId(R.id.iv_avengers_badge)).check(matches(not(isDisplayed())) );
    }

    //espresso intents / intenders.

    private void givenThereAreNoSuperHeroes() {
        when(repository.getAll()).thenReturn(Collections.<SuperHero>emptyList());
    }

    private List<SuperHero> givenThereAreSomeSuperHeroes() {
        return givenThereAreANumberOfSuperHeroes(5);
    }

    private List<SuperHero> givenThereAreANumberOfSuperHeroes(int number) {
        List<SuperHero> mockSuperHeroes = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            mockSuperHeroes.add(new SuperHero("Hero " + i, "http://lorempixel.com/400/200/", true,
                    "Desc " + i));
        }

        when(repository.getAll()).thenReturn(mockSuperHeroes);
        return mockSuperHeroes;
    }

    private List<SuperHero> givenThereAreANumberOfSuperHeroesWhereOddAreAvengers(int number) {
        List<SuperHero> mockSuperHeroes = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            mockSuperHeroes.add(
                    new SuperHero("Hero " + i, "http://lorempixel.com/400/200/", i % 2 == 0,
                            "Desc " + i));
        }

        when(repository.getAll()).thenReturn(mockSuperHeroes);
        return mockSuperHeroes;
    }

    private MainActivity startActivity() {
        return activityRule.launchActivity(null);
    }
}