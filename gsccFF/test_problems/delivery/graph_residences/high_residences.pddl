(define 
    (problem high_residences) 
    (:domain community_delivery_simplified)
    
    (:objects   
        ;delivery person
        man - delivery_person

        ;car
        ferrari - car

        ;residences
        residence0 - residence
        residence1 - residence
        residence2 - residence
        residence3 - residence
        residence4 - residence
        residence5 - residence

        ;shops
        petrol_station0 - petrol_station
        superstore0 - superstore
    )


    ;todo: put the initial state's facts and numeric values here
    (:init
        ;predicates
        (at man petrol_station0)
        (at ferrari petrol_station0)

        (owns man ferrari)
        
        
        ;functions

        ;delivery person
        (= (walk_speed man) 10)
        (= (max_walk_range man) 100)
        (= (balance man) 30000)
        
        ;storage
        (= (current_supply_amount man) 0)
        (= (total_carrying_capacity man) 10)

        (= (current_supply_amount man) 0)
        (= (total_carrying_capacity ferrari) 100)

        ;residence
        (= (supply_demand residence0) 2)
        (= (payment residence1) 40)

        (= (supply_demand residence1) 2)
        (= (payment residence0) 10)

        (= (supply_demand residence2) 2)
        (= (payment residence2) 20)
        
        (= (supply_demand residence3) 2)
        (= (payment residence3) 5)


        (= (supply_demand residence4) 2)
        (= (payment residence4) 20)


        (= (supply_demand residence5) 2)
        (= (payment residence5) 5)

        ;store
        (= (item_price superstore0) 1)

        ;car
        (= (fuel_level ferrari) 500)
        (= (max_fuel_level ferrari) 1500)
        (= (fuel_per_distance ferrari) 3)
        (= (speed ferrari) 80)

        ;petrol_station
        (= (fuel_increments petrol_station0) 100)

        ;location location
        (= (distance residence0 residence1) 100)
        (= (distance residence1 residence0) 100)

        (= (distance residence0 residence2) 100)
        (= (distance residence2 residence0) 100)

        (= (distance residence0 residence3) 100)
        (= (distance residence3 residence0) 100)

        (= (distance residence0 residence4) 100)
        (= (distance residence4 residence0) 100)

        (= (distance residence0 residence5) 100)
        (= (distance residence5 residence0) 100)

        
        (= (distance residence0 petrol_station0) 100)
        (= (distance petrol_station0 residence0) 100)

        (= (distance superstore0 residence1) 100)
        (= (distance residence1 superstore0) 100)
        
        ;global
        (= (time) 0)
    )

    ;todo: put the goal condition here
    (:goal (
        and 

            ;drive to locations
            (completed residence5)
            (completed residence4)
            (completed residence2)
            (completed residence3)
            (completed residence1)
            (completed residence0)
    ))
    (:metric minimize (time))
)