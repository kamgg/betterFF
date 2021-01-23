(define (domain community_delivery_simplified)
    (:requirements :durative-actions :numeric-fluents :duration-inequalities :typing :strips :negative-preconditions :equality)

	(:types location storage - object
        ;fixed positions in the domain
		store residence - location
        
		superstore petrol_station - store
        ;objects that can carry/hold supplies
		delivery_person car - storage
	)

    (:predicates
        (at ?s - storage ?l - location)
        
        ;to help ensure that only one delivery person can drop off at a given residence
        (assigned_to ?d - delivery_person ?r - residence)
        (assigned ?r - residence)
        (completed ?r - residence)

        ;for cars
        (owns ?d - delivery_person ?c - car)
    )

    (:functions 
        ;location location
        (distance ?loc1 ?loc2 - location)

        ;store
        (item_price ?s - store)

        ;delivery_person
        (walk_speed ?deliv_person - delivery_person)
        (max_walk_range ?deliv_person - delivery_person)
        (balance ?deliv_person - delivery_person)
        
        ;storage
        (current_supply_amount ?s - storage)
        (total_carrying_capacity ?s - storage)

        ;car
        (fuel_level ?c - car)
        (max_fuel_level ?c - car)
        (fuel_per_distance ?c - car)
        (speed ?c - car)

        ;petrol_station
        (fuel_increments ?ps - petrol_station)

        ;residence
        (supply_demand ?r - residence)

        (payment ?r - residence)
        

        ;global 
        (time)
    )
    

    ;walk to a location if it's within the allowed range
    (:durative-action walk
        :parameters (?start_loc ?end_loc - location ?deliv_person - delivery_person)
        :duration (= ?duration (/ (distance ?start_loc ?end_loc) (walk_speed ?deliv_person)))
        :condition (and 
            (at start (
                and
                    (not (= ?start_loc ?end_loc))
                    (at ?deliv_person ?start_loc)
                    (<= (distance ?start_loc ?end_loc) (max_walk_range ?deliv_person))
            ))
        )
        :effect (and 
            (at start (
                and
                   (not (at ?deliv_person ?start_loc))
            ))
            (at end (
                and
                    (at ?deliv_person ?end_loc) 
                    ;increase global time by one for every unit of time this action goes on
                    (increase (time) (/ (distance ?start_loc ?end_loc) (walk_speed ?deliv_person)))  
            ))   
        )
    )
    

    ;makes the delivery man commit to an order
    (:action accept_order
        :parameters (?res - residence ?deliv_person - delivery_person)
        :precondition (and
            (not (assigned ?res))
        )
        :effect (and 
            (assigned ?res)
            (assigned_to ?deliv_person ?res)
        )
    )

    ;lets the delivery person take payment once everything has been delivered etc
    (:action take_payment
        :parameters (?deliv_person - delivery_person ?res - residence) 
        :precondition (and 
            (not (completed ?res))
            (assigned_to ?deliv_person ?res)
            (= (supply_demand ?res) 0)
        )
        :effect (and 
            (completed ?res)
            (increase (balance ?deliv_person) (payment ?res))
            ;set the payment to 0
            (assign (payment ?res) 0)
        )
    )
    
    

    ;drop supply item
    (:action drop_supply
        :parameters (?drop_off_res - residence ?deliv_person - delivery_person)
        :precondition (and 
            (> (current_supply_amount ?deliv_person) 0)
            (> (supply_demand ?drop_off_res) 0)
            (assigned_to ?deliv_person ?drop_off_res)
            (at ?deliv_person ?drop_off_res)
            
        )
        :effect (and 
            (decrease (current_supply_amount ?deliv_person) 1)
            (decrease (supply_demand ?drop_off_res) 1)
        )
    )

    ;pick up supply item
    (:action pick_up_supply
        :parameters (?superstore - superstore ?deliv_person - delivery_person)
        :precondition (and 
            (> (total_carrying_capacity ? deliv_person) (current_supply_amount ?deliv_person))          
            (at ?deliv_person ?superstore)
            (>= (balance ?deliv_person) (item_price ?superstore)) 
        )
        :effect (and 
            (increase (current_supply_amount ?deliv_person) 1) 
            (decrease (balance ?deliv_person) (item_price ?superstore))
        )
    )

    ;load a supply item into the car
    (:action load_supply_into_car
        :parameters (?the_car - car ?deliv_person - delivery_person ?loc - location)
        :precondition (and
            ;must be in the same location
            (at ?deliv_person ?loc)
            (at ?the_car ?loc)

            (owns ?deliv_person ?the_car)
            (> (total_carrying_capacity ?the_car) (current_supply_amount ?the_car))
            (> (current_supply_amount ?deliv_person) 0)
        )
        :effect (and 
            (decrease (current_supply_amount ?deliv_person) 1)
            (increase (current_supply_amount ?the_car) 1)
        )
    )

    ;unload a supply item from the car 
    (:action unload_supply_from_car
        :parameters (?the_car - car ?deliv_person - delivery_person ?loc - location)
        :precondition (and
            ;must be in the same location
            (at ?deliv_person ?loc)
            (at ?the_car ?loc)

            (owns ?deliv_person ?the_car)
            (> (total_carrying_capacity ?deliv_person) (current_supply_amount ?deliv_person))
            (> (current_supply_amount ?the_car) 0)
        )
        :effect (and
            (increase (current_supply_amount ?deliv_person) 1)
            (decrease (current_supply_amount ?the_car) 1) 
        )
    )

    ;load fuel into the car
    (:action fuel_car
        :parameters(?the_car - car ?deliv_person - delivery_person ?petrol_station_store - petrol_station)
        :precondition(and
            ;car and person must be at a petrol station
            (at ?the_car ?petrol_station_store)
            (at ?deliv_person ?petrol_station_store)
            
            (owns ?deliv_person ?the_car)
            (< (+ (fuel_level ?the_car) (fuel_increments ?petrol_station_store)) (max_fuel_level ?the_car)) 
            (> (balance ?deliv_person) (item_price ?petrol_station_store))
        )
        :effect(and
            (decrease (balance ?deliv_person) (item_price ?petrol_station_store))
            (increase (fuel_level ?the_car) (fuel_increments ?petrol_station_store))
        )
    )

    ;drive car to a location
    (:durative-action drive
        :parameters (?start_loc ?end_loc - location ?deliv_person - delivery_person ?the_car - car)
        :duration (= ?duration (/ (distance ?start_loc ?end_loc) (speed ?the_car)))
        :condition (and 
            (at start (
                and
                    ;needs to have no items on him
                    (= (current_supply_amount ?deliv_person) 0)

                    (owns ?deliv_person ?the_car)
                    ;car and person must be at the start location
                    (at ?the_car ?start_loc)
                    (at ?deliv_person ?start_loc)
                    ;should have enough fuel
                    (> (fuel_level ?the_car) (* (distance ?start_loc ?end_loc) (fuel_per_distance ?the_car)))

            ))
        )
        :effect (and 
            (at start (
                and
                   (not (at ?deliv_person ?start_loc))
                   (not (at ?the_car ?start_loc))
                   (decrease (fuel_level ?the_car) (* (distance ?start_loc ?end_loc) (fuel_per_distance ?the_car)))
            ))
            (at end (
                and
                    (at ?deliv_person ?end_loc) 
                    (at ?the_car ?end_loc) 
                    ;increase global time by one for every unit of time this action goes on
                    (increase (time) (/ (distance ?start_loc ?end_loc) (speed ?the_car)))  
            ))   
        )
    )
)
