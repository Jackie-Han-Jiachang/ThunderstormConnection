package bnds.chinese.repository;

import bnds.chinese.model.AppState;

public interface EventRepository {
    AppState load();
    void save(AppState state);
}
